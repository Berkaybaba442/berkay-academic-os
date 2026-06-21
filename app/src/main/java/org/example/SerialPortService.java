package org.example;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SerialPortService {

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private InputStream inputStream;
    private OutputStream outputStream;
    private Thread readerThread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Path connectedPort;
    private int connectedBaudRate;
    private Consumer<String> dataConsumer;
    private Consumer<String> errorConsumer;

    public synchronized List<String> listAvailablePorts() {
        Set<String> ports = new LinkedHashSet<>();

        addGlobMatches(ports, "/dev/ttyUSB*");
        addGlobMatches(ports, "/dev/ttyACM*");
        addGlobMatches(ports, "/dev/ttyS*");
        addGlobMatches(ports, "/dev/rfcomm*");
        addGlobMatches(ports, "/dev/cu.*");
        addGlobMatches(ports, "/dev/tty.*");
        addSerialByIdMatches(ports);

        List<String> sorted = new ArrayList<>(ports);
        sorted.sort(Comparator.naturalOrder());
        return sorted;
    }

    public synchronized void connect(String portName, int baudRate, Consumer<String> dataConsumer) throws IOException {
        connect(portName, baudRate, dataConsumer, null);
    }

    public synchronized void connect(
            String portName,
            int baudRate,
            Consumer<String> dataConsumer,
            Consumer<String> errorConsumer
    ) throws IOException {
        disconnect();

        if (portName == null || portName.isBlank()) {
            throw new IOException("Port adı boş olamaz.");
        }
        if (baudRate <= 0) {
            throw new IOException("Baud rate geçersiz: " + baudRate);
        }

        Path portPath = Paths.get(portName).normalize();
        validatePort(portPath);
        configurePort(portPath, baudRate);

        this.inputStream = new BufferedInputStream(Files.newInputStream(portPath));
        this.outputStream = new BufferedOutputStream(Files.newOutputStream(portPath));
        this.connectedPort = portPath;
        this.connectedBaudRate = baudRate;
        this.dataConsumer = dataConsumer;
        this.errorConsumer = errorConsumer;
        this.running.set(true);

        readerThread = new Thread(this::readLoop, "berkay-academic-os-uart-reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public synchronized boolean isConnected() {
        return running.get() && connectedPort != null && inputStream != null && outputStream != null;
    }

    public synchronized String getConnectedPortName() {
        return connectedPort == null ? null : connectedPort.toString();
    }

    public synchronized int getConnectedBaudRate() {
        return connectedBaudRate;
    }

    public synchronized void send(String text) throws IOException {
        if (!isConnected()) {
            throw new IOException("Port bağlı değil.");
        }
        if (text == null) {
            return;
        }

        outputStream.write(text.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    public synchronized void sendBytes(byte[] bytes) throws IOException {
        if (!isConnected()) {
            throw new IOException("Port bağlı değil.");
        }
        if (bytes == null || bytes.length == 0) {
            return;
        }

        outputStream.write(bytes);
        outputStream.flush();
    }

    public synchronized void disconnect() throws IOException {
        running.set(false);

        IOException closeError = null;

        if (readerThread != null) {
            readerThread.interrupt();
            readerThread = null;
        }

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ex) {
                closeError = ex;
            } finally {
                inputStream = null;
            }
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException ex) {
                if (closeError == null) {
                    closeError = ex;
                }
            } finally {
                outputStream = null;
            }
        }

        connectedPort = null;
        connectedBaudRate = 0;
        dataConsumer = null;
        errorConsumer = null;

        if (closeError != null) {
            throw closeError;
        }
    }

    private void readLoop() {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        while (running.get()) {
            try {
                InputStream currentInput = inputStream;
                if (currentInput == null) {
                    return;
                }

                int read = currentInput.read(buffer);
                if (read > 0) {
                    Consumer<String> consumer = dataConsumer;
                    if (consumer != null) {
                        consumer.accept(new String(buffer, 0, read, StandardCharsets.UTF_8));
                    }
                } else if (read < 0) {
                    notifyError("Port veri akışı kapandı.");
                    running.set(false);
                }
            } catch (IOException ex) {
                if (running.get()) {
                    notifyError(ex.getMessage() == null ? "UART okuma hatası" : ex.getMessage());
                }
                running.set(false);
            }
        }
    }

    private void notifyError(String message) {
        Consumer<String> consumer = errorConsumer;
        if (consumer != null) {
            consumer.accept(message);
        }
    }

    private void validatePort(Path portPath) throws IOException {
        if (!Files.exists(portPath)) {
            throw new IOException("Port bulunamadı: " + portPath);
        }
        if (Files.isDirectory(portPath)) {
            throw new IOException("Seçilen yol port değil, klasör: " + portPath);
        }
        if (!Files.isReadable(portPath) || !Files.isWritable(portPath)) {
            throw new IOException("Port için okuma/yazma izni yok: " + portPath
                    + "\nLinux için kullanıcıyı dialout grubuna eklemeyi deneyebilirsin: sudo usermod -aG dialout $USER");
        }
    }

    private void configurePort(Path portPath, int baudRate) throws IOException {
        if (!isUnixLike()) {
            return;
        }

        List<String> command = List.of(
                "stty",
                "-F",
                portPath.toString(),
                String.valueOf(baudRate),
                "cs8",
                "-cstopb",
                "-parenb",
                "-ixon",
                "-ixoff",
                "-echo",
                "raw",
                "min",
                "0",
                "time",
                "1"
        );

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output;
        try {
            output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Port yapılandırılamadı: " + (output.isBlank() ? "stty hata kodu " + exitCode : output));
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Port yapılandırma işlemi kesildi.", ex);
        }
    }

    private boolean isUnixLike() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        return osName.contains("linux") || osName.contains("mac") || osName.contains("nix") || osName.contains("nux");
    }

    private void addGlobMatches(Set<String> ports, String pattern) {
        Path patternPath = Paths.get(pattern);
        Path parent = patternPath.getParent();
        Path fileName = patternPath.getFileName();

        if (parent == null || fileName == null || !Files.isDirectory(parent)) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent, fileName.toString())) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    ports.add(path.toString());
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void addSerialByIdMatches(Set<String> ports) {
        Path serialById = Paths.get("/dev/serial/by-id");
        if (!Files.isDirectory(serialById)) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(serialById)) {
            for (Path path : stream) {
                ports.add(path.toString());
            }
        } catch (IOException ignored) {
        }
    }
}