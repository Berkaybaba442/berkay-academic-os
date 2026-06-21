Sen tam donanımlı bir Java App MühendisisİN. Bunca zamandır binlerce kullanıcı için kendilerine özel uygulamalar geliştirdin. Bunu baz alarak aşağıdaki planı değerlendir.

# Berkay Academic OS — Tam Durum Dokümanı
_Güncel teknik özet, yapılanlar, mevcut mimari, dosya düzeni, entegrasyonlar, hatalar, bekleyen işler ve sonraki adımlar_

**Güncelleme durumu:** Bu sürümde **Aşama A, Aşama B, Aşama C, Aşama D ve Aşama E tamamlandı.** Sıradaki aktif aşama **Aşama F — Referans modülü zenginleştirme** olarak belirlendi.

**Son düzenleme kapsamı:**
- Aşama C — Belge Merkezi Drag & Drop import eklendi.
- Aşama D — UART Konsolu ürünleştirildi.
- Aşama E — Preview polish tamamlandı.
- Yol haritası baştan düzenlendi ve eski “Aşama C sıradaki aktif aşama” bilgisi güncellendi.

---

## 1. Proje Özeti

**Berkay Academic OS**, JavaFX tabanlı, modüler, config odaklı bir masaüstü uygulaması olarak ilerliyor.

Ana amaç:
- tek bir masaüstü uygulaması içinde farklı modülleri çalıştırmak
- modülleri `config.json` üzerinden tanımlamak
- sidebar üzerinden modül açmak
- merkez alanda modül bazlı içerik göstermek
- dosya yönetimi, not alma, görev planlama, referans saklama, UART, takvim ve preview araçlarını aynı çatıya toplamak

Bu doküman:
- şu ana kadar yapılan işleri
- projedeki dosya mantığını
- hangi dosyada ne olduğunu
- hangi dosyaların hangi amaçla değiştirildiğini
- tamamlanan aşamaları
- kalan teknik borçları
- bundan sonra yapılacak işleri  
ayrıntılı şekilde toparlar.

---

# 2. Genel Mimari

Uygulama genel olarak şu akışta çalışıyor:

```text
App.java
 ├─ ConfigLoader.java → config.json yükler
 ├─ SidebarBuilder.java → soldaki modül butonlarını üretir
 ├─ ModuleActionHandler.java → modül tipine/id’sine göre yönlendirme yapar
 ├─ WebModuleView.java → dahili web + not ekranı
 ├─ FileExplorerView.java → Belge Merkezi
 │   ├─ FileOperationService.java → dosya işlemleri + drag-drop import
 │   ├─ PreviewService.java → preview karar mekanizması
 │   └─ PreviewOptions.java → preview config seçenekleri
 ├─ CalendarModuleView.java → Takvim + görev planı
 │   ├─ GoogleCalendarService.java → Google Calendar event servis katmanı
 │   └─ GoogleCalendarOAuthService.java → OAuth bağlantı katmanı
 ├─ ReferenceLibraryView.java → Referans kütüphanesi
 ├─ UartConsoleView.java → UART paneli
 │   └─ SerialPortService.java → seri port servis katmanı
 └─ ScriptModuleRunner.java → script çalıştırma
```

Temel prensip:
- `App.java` her şeyi tek başına yapmasın.
- Her modül kendi view/service sınıfına ayrılsın.
- Config üzerinden büyüyebilen yapı olsun.
- Veri gereken modüller dosya tabanlı kalıcılığa sahip olsun.
- UI işlemleri ile uzun süren disk/IO işlemleri ayrıştırılsın.
- Kullanıcı hatalarında uygulama çökmesin, durum mesajı üretsin.

---

# 3. Proje Klasör Yapısı (Mantıksal Şema)

```text
berkay-academic-os/
├─ app/
│  ├─ build.gradle
│  └─ src/
│     └─ main/
│        ├─ java/
│        │  └─ org/example/
│        │     ├─ App.java
│        │     ├─ AppConfig.java
│        │     ├─ ConfigLoader.java
│        │     ├─ SidebarBuilder.java
│        │     ├─ ModuleActionHandler.java
│        │     ├─ ModuleItem.java
│        │     ├─ AppSettings.java
│        │     ├─ UiSettings.java
│        │     ├─ StorageSettings.java
│        │     ├─ IntegrationSettings.java
│        │     ├─ ModuleFeatures.java
│        │     ├─ ModuleDataSources.java
│        │     ├─ WebModuleView.java
│        │     ├─ ScriptModuleRunner.java
│        │     ├─ FileExplorerView.java
│        │     ├─ FileOperationService.java
│        │     ├─ PreviewService.java
│        │     ├─ PreviewOptions.java
│        │     ├─ ExplorerItem.java
│        │     ├─ FavoriteLocation.java
│        │     ├─ FileExplorerOptions.java
│        │     ├─ UartConsoleView.java
│        │     ├─ SerialPortService.java
│        │     ├─ CalendarModuleView.java
│        │     ├─ TaskItem.java
│        │     ├─ TaskStorageService.java
│        │     ├─ CalendarEventItem.java
│        │     ├─ GoogleCalendarService.java
│        │     ├─ GoogleCalendarOAuthService.java
│        │     ├─ ReferenceLibraryView.java
│        │     └─ ReferenceLibrary ile ilgili iç yardımcı sınıflar
│        └─ resources/
│           └─ config.json
│
├─ data/
│  ├─ notes/
│  ├─ cache/
│  ├─ downloads/
│  ├─ tasks.json
│  └─ reference-library/
│     ├─ index.json
│     └─ topics/
│        ├─ elektronik/
│        ├─ matematik/
│        └─ embedded/
│
├─ config/
│  ├─ google-calendar-credentials.json
│  ├─ google-calendar-tokens/
│  ├─ google-drive-credentials.json
│  └─ google-drive-tokens/
│
└─ scripts/
   └─ test_script.py
```

Notlar:
- `config/` klasörü sistem tarafından hazır gelmez; proje kökünde manuel oluşturulabilir.
- `data/` içindeki dosyalar runtime sırasında oluşabilir.
- `reference-library/topics/` altına markdown içerikler yazılır.
- `tasks.json` görev planı kalıcılığı için kullanılır.
- Google Calendar tokenları `config/google-calendar-tokens/` altında tutulur.

---

# 4. Şu Ana Kadar Yapılanlar — Ayrıntılı

## 4.1 Uygulama iskeleti kuruldu

Yapılanlar:
- JavaFX ana pencere oluşturuldu.
- Üst başlık barı eklendi.
- Sol sidebar eklendi.
- Alt durum çubuğu eklendi.
- Merkez alan modül gösterme mantığı kuruldu.

Ana dosya:
- `app/src/main/java/org/example/App.java`

---

## 4.2 Config tabanlı mimari kuruldu

Yapılanlar:
- `config.json` dosyası resource olarak okunuyor.
- Uygulama ayarları config üzerinden geliyor.
- Modüller config içinden geliyor.

İlgili dosyalar:
- `AppConfig.java`
- `ConfigLoader.java`
- `App.java`

Önerilen teknik yaklaşım:
- Config büyüdükçe `FAIL_ON_UNKNOWN_PROPERTIES = false` yaklaşımı korunmalı.
- Yeni modül seçenekleri eklense bile eski config dosyaları uygulamayı düşürmemeli.

---

## 4.3 Sidebar üretimi ayrıştırıldı

Yapılanlar:
- Sidebar üretimi `App.java` içinden çıkarıldı.
- `SidebarBuilder` sınıfına taşındı.

Faydası:
- `App.java` sadeleşti.
- Modül butonlarıyla ilgili mantık ayrıldı.

İlgili dosya:
- `SidebarBuilder.java`

---

## 4.4 Modül yönlendirme ayrıştırıldı

Yapılanlar:
- Modül açma kararı `App.java` içinden çıkarılıp `ModuleActionHandler` içine taşındı.
- `internal`, `external_web`, `script`, `file_explorer`, `web` ayrımları netleşti.

İlgili dosya:
- `ModuleActionHandler.java`

Faydası:
- Modül açma mantığı merkezileşti.
- Yeni modül eklemek daha kolay hale geldi.

---

## 4.5 Web modülü + not alma sistemi çalışır hale geldi

Yapılanlar:
- Dahili web ekranı kuruldu.
- Not alanı ile beraber çalışacak şekilde bölünmüş ekran yapısı oluşturuldu.
- Modül bazlı not kaydetme eklendi.

İlgili dosyalar:
- `WebModuleView.java`
- `App.java`
- `FileOperationService.java`

Notların yolu:

```text
data/notes/<module_adi>.txt
```

---

## 4.6 Script modülü ayrıldı

Yapılanlar:
- Script çalıştırma mantığı `ScriptModuleRunner` içine taşındı.
- Config içinden interpreter ve hedef script çalıştırılabiliyor.

İlgili dosya:
- `ScriptModuleRunner.java`

Örnek kullanım:
- `scripts/test_script.py`

---

## 4.7 Belge Merkezi oluşturuldu

Yapılanlar:
- Klasör listeleme
- Klasör açma
- Metin dosyası açma
- Dosya kaydetme
- Klasör oluşturma
- Dosya oluşturma
- Yeniden adlandırma
- Silme
- Harici uygulamada açma
- Favoriler
- Preview davranışı

İlgili dosyalar:
- `FileExplorerView.java`
- `FileOperationService.java`
- `PreviewService.java`
- `PreviewOptions.java`
- `ExplorerItem.java`
- `FavoriteLocation.java`
- `FileExplorerOptions.java`

---

## 4.8 Belge Merkezi performans iyileştirildi

Yapılanlar:
- Klasör yükleme async mantığa taşındı.
- UI donmaları azaltıldı.
- Eski yükleme isteğinin yeni sonucu bozması engellendi.
- Seçim davranışları temizlendi.
- Çift tıkla açma mantığına geçildi.

Sonuç:
- Klasör gezme daha stabil oldu.
- Seçim listener kaynaklı çökme riski azaldı.

---

## 4.9 Recursive delete eklendi

Yapılanlar:
- Dolu klasörler de silinebilir hale getirildi.
- `Files.walk(...)` ile klasör içeriği tersten silinecek şekilde düzenlendi.

İlgili dosya:
- `FileOperationService.java`

---

## 4.10 Takvim modülü placeholder’dan çıkarıldı

Yapılanlar:
- Görev listesi
- Filtreleme
- Detay gösterimi
- Görev ekleme
- Görev silme
- Tamamlandı yapma

İlgili dosyalar:
- `CalendarModuleView.java`
- `TaskItem.java`

---

## 4.11 Takvim modülüne veri kalıcılığı eklendi

Yapılanlar:
- Görevler JSON’a yazılıyor.
- Açılışta görevler JSON’dan okunuyor.

İlgili dosya:
- `TaskStorageService.java`

Veri yolu:

```text
data/tasks.json
```

---

## 4.12 Referans modülü gerçek panele dönüştürüldü

Yapılanlar:
- Kategori listesi
- Konu listesi
- Detay/edit alanı
- Yeni konu ekleme
- Kaydetme
- Silme
- Diskten yeniden yükleme
- Veri dosyası + markdown içerik mantığı

İlgili dosya:
- `ReferenceLibraryView.java`

Mevcut veri şeması:

```text
data/reference-library/index.json
data/reference-library/topics/<kategori>/<konu>.md
```

---

## 4.13 UART modülü ilk iskelet olarak oluşturuldu

İlk durumda:
- Basit port tarama
- Port seçme
- Baud seçme
- Bağlan/kes butonları
- Log alanı
- Metin gönderme alanı vardı.

Bu modül Aşama D’de ürünleşmiş hale getirildi.

İlgili dosyalar:
- `UartConsoleView.java`
- `SerialPortService.java`

---

## 4.14 Preview config entegrasyonu başlatıldı

İlk durumda:
- `PreviewService` dosya uzantısına göre metin/görsel/harici/desteklenmeyen kararını veriyordu.
- `PreviewOptions` allowed/openExternally/maxSize gibi temel config alanlarını taşıyordu.

Bu modül Aşama E’de metadata, PDF davranışı ve daha net karar sistemiyle cilalandı.

İlgili dosyalar:
- `PreviewService.java`
- `PreviewOptions.java`
- `FileExplorerView.java`

---

## 4.15 Takvim modülünde sekmeli yapı kurgulandı

Yapılanlar:
- Üstte ay görünümü
- Altta görev planı yerine sekmeli yapı
- Takvim ve görev planı ayrı sekmeler
- Görev planı taşma sorununun azaltılması

Yeni mantık:
- **Takvim** sekmesi
- **Görev Planı** sekmesi

---

## 4.16 Google Calendar entegrasyonu denendi ve stabilize edildi

Aşamalar:
1. Önce ICS tabanlı kolay entegrasyon düşünüldü.
2. Sonra gerçek OAuth 2.0 masaüstü akışına geçildi.
3. Başlangıçta auth çalışmasının çökme riski yarattığı görüldü.
4. OAuth sadece butonla tetiklenecek yapıya çekildi.
5. `GoogleCalendarService` ile `GoogleCalendarOAuthService` ayrıştırıldı.

İlgili dosyalar:
- `CalendarEventItem.java`
- `GoogleCalendarService.java`
- `GoogleCalendarOAuthService.java`
- `CalendarModuleView.java`

Config / dosya yolu:

```text
config/google-calendar-credentials.json
config/google-calendar-tokens/
```

---

## 4.17 Takvimde çift tık davranışı düzeltildi

İstek:
- Takvimde güne tıklayınca anında görev planına geçmesin.
- **Çift tıklanınca** görev planı sekmesine geçsin.

Yapılan çözüm:
- Tek tıkta sadece seçili gün güncelleniyor.
- Çift tıkta görev planı sekmesine geçiliyor.
- Seçili gün filtresi uygulanıyor.
- Takvim rebuild’i ilk tıkta yapılmıyor.

---

## 4.18 Aşama A tamamlandı — Google Calendar stabilizasyonu

Aşama A kapsamında Google Calendar entegrasyonundaki çökme riski ve UI donma riski azaltıldı.

Tamamlananlar:
- Eski `urn:ietf:wg:oauth:2.0:oob` tabanlı manuel kod yapıştırma akışı kaldırıldı.
- `TextInputDialog` ile authorization code alma yaklaşımı terk edildi.
- OAuth akışı `LocalServerReceiver` tabanlı lokal callback yapısına taşındı.
- `GoogleCalendarService.connect()` işlemi JavaFX UI thread üzerinden çıkarılıp arka plan `Task` yapısına alındı.
- Google Calendar event çekme işlemi de arka planda çalışacak hale getirildi.
- Ay değiştirirken bağlantı yoksa yeniden OAuth tetiklenmemesi garanti altına alındı.
- Google Calendar event sonuçları ay bazlı cache ile saklanmaya başlandı.
- Hızlı ay geçişlerinde eski yükleme sonucunun yeni ay görünümünü bozmasını engellemek için sıra kontrolü eklendi.
- Bağlantı, yükleme ve hata durumları UI bilgi etiketinde daha okunur hale getirildi.

İlgili dosyalar:
- `CalendarModuleView.java`
- `GoogleCalendarService.java`
- `GoogleCalendarOAuthService.java`
- `CalendarEventItem.java`

Ek dependency ihtiyacı:

```gradle
implementation 'com.google.oauth-client:google-oauth-client-jetty:1.36.0'
```

---

## 4.19 Aşama B tamamlandı — Belge Merkezi yetki kontrolü

Aşama B kapsamında Belge Merkezi için `readOnly` ve işlem izinleri config/options katmanına bağlandı.

Tamamlananlar:
- `FileExplorerOptions` içine izin kararlarını merkezi veren yardımcı metotlar eklendi.
- `readOnly` aktifken dosya/klasör oluşturma, yeniden adlandırma, silme ve kaydetme işlemleri kapatıldı.
- Create/rename/delete butonlarının görünürlüğü ve kullanılabilirliği config izinlerine bağlandı.
- Metin editörü `readOnly` modda düzenlenemez hale getirildi.
- Kaydetme butonu `readOnly` modda gizlendi ve ayrıca guard kontrolü eklendi.
- `showPreviewPane: false` ise sağ önizleme/editor panelinin gizlenmesi sağlandı.
- `FileOperationService` içine izin kontrollü overload metotlar eklendi.
- Dosya/klasör adlarında `/`, `\`, `.`, `..` gibi riskli/geçersiz isimler engellendi.
- Kullanıcıya Belge Merkezi üstünde izin özeti gösterilecek hale getirildi.

İlgili dosyalar:
- `FileExplorerOptions.java`
- `FileExplorerView.java`
- `FileOperationService.java`
- `config.json`

Eklenen ana yardımcı metotlar:

```java
canCreateFolder()
canCreateFile()
canRename()
canDelete()
canEditFiles()
canDragDropImport()
permissionSummary()
```

---

## 4.20 Aşama C tamamlandı — Belge Merkezi Drag & Drop import

Aşama C kapsamında Belge Merkezi’ne dışarıdan dosya/klasör sürükleyip bırakma desteği eklendi.

Tamamlananlar:
- `FileExplorerView` içine JavaFX `drag-over`, `drag-entered`, `drag-exited` ve `drag-dropped` event akışları eklendi.
- Kullanıcıya görünür import alanı/izin etiketi eklendi.
- Dosya veya klasörler mevcut açık klasöre import edilecek hale getirildi.
- Import işlemi UI thread dışında `Task` ile arka planda çalışacak şekilde bağlandı.
- Import sırasında liste geçici olarak disable edilir ve durum çubuğunda “Import ediliyor...” mesajı gösterilir.
- Import tamamlandığında klasör listesi otomatik yenilenir.
- `readOnly` ve `allowDragDropImport` izinlerine tam uyum sağlandı.
- `FileOperationService` içine izin kontrollü `importPaths(...)` overload metodu eklendi.
- Klasör importu için recursive copy desteği eklendi.
- Aynı isimli dosya/klasör varsa otomatik çakışma çözümü eklendi: `dosya.txt`, `dosya (1).txt`, `dosya (2).txt`.
- Bir klasörün kendi içine veya alt klasörüne import edilmesi güvenlik nedeniyle engellendi.
- Import sonucu kullanıcıya dosya/klasör sayısı olarak gösterilir.

İlgili dosyalar:
- `FileExplorerView.java`
- `FileOperationService.java`
- `FileExplorerOptions.java`
- `config.json`

Test beklentisi:
- `allowDragDropImport: true` ve `readOnly: false` olduğunda dosya/klasör sürükle bırak çalışır.
- `readOnly: true` olduğunda import reddedilir.
- `allowDragDropImport: false` olduğunda import reddedilir.
- Aynı isimli dosyalar eski dosyanın üzerine yazılmaz, yeni adla kopyalanır.
- Büyük klasör importunda UI donmaz.
- Import sonrası liste otomatik yenilenir.

---

## 4.21 Aşama D tamamlandı — UART ürünleştirme

Aşama D kapsamında UART Konsolu iskelet seviyesinden günlük kullanıma daha uygun, daha stabil bir araca dönüştürüldü.

### `UartConsoleView.java` tarafında yapılanlar

- Port seçim alanı düzenlenebilir hale getirildi.
- Baud rate seçim alanı düzenlenebilir hale getirildi.
- Yaygın baud rate değerleri eklendi: `9600`, `19200`, `38400`, `57600`, `115200`, `230400`, `460800`, `921600`.
- Port tarama işlemi JavaFX UI thread dışına alındı.
- Bağlanma işlemi JavaFX UI thread dışına alındı.
- Bağlantı durumuna göre butonlar otomatik açılıp kapanır hale getirildi.
- Port bağlıyken port ve baud alanlarının değiştirilmesi engellendi.
- Gönderme alanı Enter tuşu ile de veri gönderebilir hale getirildi.
- Satır sonu seçimi eklendi: `Yok`, `LF`, `CR`, `CRLF`.
- Timestamp seçeneği eklendi.
- Auto-scroll seçeneği eklendi.
- Log temizleme butonu korundu.
- Log boyutu sınırlandı, çok uzun kullanımda `TextArea` aşırı büyümesi engellendi.
- TX / UART / SİSTEM / HATA ayrımı eklendi.

### `SerialPortService.java` tarafında yapılanlar

- Port keşfi `ProcessBuilder + ls` bağımlılığından çıkarıldı.
- `DirectoryStream` tabanlı glob port tarama yapısı kuruldu.
- Linux için şu port yolları desteklendi:
  - `/dev/ttyUSB*`
  - `/dev/ttyACM*`
  - `/dev/ttyS*`
  - `/dev/rfcomm*`
  - `/dev/serial/by-id/*`
- macOS benzeri cihazlar için şu yollar da taramaya dahil edildi:
  - `/dev/cu.*`
  - `/dev/tty.*`
- Port bağlanmadan önce varlık kontrolü yapıldı.
- Port bağlanmadan önce okuma/yazma izin kontrolü yapıldı.
- İzin hatasında Linux için `dialout` grubu uyarısı verildi.
- `stty` yapılandırması shell string yerine argüman listesiyle çalıştırıldı.
- Shell injection riski azaltıldı.
- `raw`, `cs8`, `-cstopb`, `-parenb`, `-ixon`, `-ixoff`, `-echo`, `min 0`, `time 1` ayarları eklendi.
- Veri okuma işlemi ayrı daemon reader thread üzerinde çalışır hale getirildi.
- Veri callback ve hata callback ayrıştırıldı.
- `sendBytes(byte[])` metodu eklendi.
- `getConnectedBaudRate()` metodu eklendi.
- `disconnect()` akışı daha kontrollü hale getirildi.

İlgili dosyalar:
- `UartConsoleView.java`
- `SerialPortService.java`

Test beklentisi:
- ESP32 / Arduino / STM32 USB bağlantısı takılıyken `Portları Tara` portları göstermeli.
- Doğru port ve baud seçilip `Bağlan` denince durum etiketi bağlı portu göstermeli.
- Mikrodenetleyiciden gelen UART verisi log paneline düşmeli.
- Metin gönderirken seçilen satır sonu tipi uygulanmalı.
- Linux izin hatasında `dialout` uyarısı anlamlı şekilde görünmeli.

Linux izin notu:

```bash
sudo usermod -aG dialout $USER
```

Bu komuttan sonra oturumu kapatıp yeniden açmak gerekir.

---

## 4.22 Aşama E tamamlandı — Preview polish

Aşama E kapsamında Belge Merkezi içindeki önizleme sistemi daha kontrollü, okunur ve config ile uyumlu hale getirildi.

### `PreviewOptions.java` tarafında yapılanlar

Yeni / iyileştirilen alanlar:
- `allowedExtensions`
- `openExternallyExtensions`
- `maxPreviewSizeBytes`
- `maxImagePreviewSizeBytes`
- `showFileMetadata`
- `previewTextFiles`
- `previewImageFiles`
- `openPdfExternally`

Önemli davranışlar:
- Uzantılar otomatik normalize edilir.
- `.txt` ve `txt` aynı kabul edilir.
- Boş `allowedExtensions` listesi, varsayılan desteklenen türlerin serbest olduğu anlamına gelir.
- `openExternallyExtensions` içine yazılan uzantılar dahili preview yerine harici uygulamaya yönlendirilir.

### `PreviewService.java` tarafında yapılanlar

Eklenenler:
- Merkezi `decide(...)` metodu.
- `PreviewDecision` sonucu.
- `PDF` preview modu.
- Metin ve görsel için ayrı boyut kontrolü.
- Metadata metni üreten `buildMetadataText(...)` metodu.
- Daha geniş metin uzantısı desteği.
- Daha açıklayıcı hata / karar mesajları.

Desteklenen ana modlar:
- `EDITABLE_TEXT`
- `IMAGE`
- `PDF`
- `EXTERNAL`
- `UNSUPPORTED`

### `FileExplorerView.java` tarafında yapılanlar

Eklenenler:
- Preview açılırken artık doğrudan `resolvePreviewMode(...)` yerine `decide(...)` kullanılır.
- Metin dosyalarında üstte metadata, altta editor gösterilir.
- Görsel dosyalarında metadata + scroll destekli görsel alanı gösterilir.
- PDF dosyalarında bilgi kartı ve harici açma butonu gösterilir.
- Desteklenmeyen dosyalarda dosya bilgisi ve neden önizlenemediği gösterilir.
- Harici açılacak dosyalarda bilgi kartı + harici açma butonu gösterilir.

İlgili dosyalar:
- `FileExplorerView.java`
- `PreviewOptions.java`
- `PreviewService.java`

Örnek config:

```json
"previewOptions": {
  "allowedExtensions": [".txt", ".md", ".json", ".csv", ".java", ".png", ".jpg", ".pdf"],
  "openExternallyExtensions": [".docx", ".xlsx", ".pptx"],
  "maxPreviewSizeBytes": 2097152,
  "maxImagePreviewSizeBytes": 10485760,
  "showFileMetadata": true,
  "previewTextFiles": true,
  "previewImageFiles": true,
  "openPdfExternally": true
}
```

Test beklentisi:
- `.txt`, `.md`, `.json`, `.java` gibi bir dosyaya çift tıklanınca sağ panelde metadata ve içerik görünmeli.
- `readOnly` açıksa editör düzenlenemez olmalı.
- `.png`, `.jpg`, `.jpeg`, `.webp` gibi görseller metadata + scroll alanıyla görünmeli.
- `.pdf` dosyasında bilgi kartı ve harici açma butonu çıkmalı.
- Büyük metin dosyası editöre yüklenmemeli; bilgi kartı gösterilmeli.
- `openExternallyExtensions` içine eklenen uzantılar dahili preview yerine harici açma akışına gitmeli.

---

# 5. Mevcut Önemli Dosyalar ve Ne İşe Yaradıkları

## 5.1 Ana uygulama

### `app/src/main/java/org/example/App.java`

Görevleri:
- Uygulamayı başlatır.
- Config yükler.
- Root layout kurar.
- Sidebar oluşturur.
- Modül açma akışını başlatır.
- Web/not, belge merkezi, takvim gibi ana view geçişlerini yönetir.

---

## 5.2 Config tarafı

### `app/src/main/java/org/example/AppConfig.java`
- Config’in kök modelidir.
- `app`, `ui`, `storage`, `integrations`, `modules` alanlarını taşır.

### `app/src/main/java/org/example/ConfigLoader.java`
- `resources/config.json` dosyasını yükler.
- JSON → Java model dönüşümünü yapar.
- Bilinmeyen alanları tolere edecek şekilde ayarlanması önerilir.

---

## 5.3 Modül yönlendirme

### `app/src/main/java/org/example/ModuleActionHandler.java`
- Internal modülleri açar.
- External web modüllerini dış tarayıcıda açar.
- Script modülünü çalıştırır.
- `file_explorer`, `calendar`, `reference`, `uart` gibi modülleri ilgili view sınıflarına yönlendirir.

---

## 5.4 Web modülü

### `app/src/main/java/org/example/WebModuleView.java`
- Dahili tarayıcı ve not panelini oluşturur.
- Modül bazlı not alanı ile web içeriğini birlikte gösterir.

---

## 5.5 Belge Merkezi

### `app/src/main/java/org/example/FileExplorerView.java`
Görevleri:
- Dosya/kapsam görünümü
- Klasör gezinme
- Preview paneli
- Dosya açma/kaydetme/işlem arayüzü
- `readOnly` ve işlem izinlerine göre buton/editor davranışı
- `showPreviewPane` ayarına göre sağ panel görünürlüğü
- Drag & Drop import UI akışı
- Preview metadata / bilgi kartı gösterimi

### `app/src/main/java/org/example/FileOperationService.java`
Görevleri:
- Dosya sistemi işlemleri
- İzin kontrollü dosya/klasör oluşturma, kaydetme, silme ve yeniden adlandırma overload metotları
- Riskli dosya/klasör adlarını engelleyen doğrulama
- Recursive delete
- Drag & Drop import için dosya/klasör kopyalama
- Aynı isim çakışmalarında otomatik yeni ad üretme

### `app/src/main/java/org/example/PreviewService.java`
Görevleri:
- Dosya preview tür kararları
- Metin/görsel/PDF/harici/desteklenmeyen kararları
- Boyut ve uzantı kontrolleri
- Metadata metni üretme
- `PreviewDecision` ile UI’a açıklamalı karar döndürme

### `app/src/main/java/org/example/PreviewOptions.java`
Görevleri:
- Preview ayarları
- İzin verilen uzantılar
- Harici açılacak uzantılar
- Metin ve görsel preview boyut limitleri
- Metadata gösterme ayarı
- PDF davranışı

### `app/src/main/java/org/example/FavoriteLocation.java`
- Favori yol yapısı.

### `app/src/main/java/org/example/ExplorerItem.java`
- Listede gösterilen dosya/klasör öğe modeli.

### `app/src/main/java/org/example/FileExplorerOptions.java`
Görevleri:
- Belge Merkezi varsayılan kök klasörü
- `readOnly` ve işlem izinleri
- create/rename/delete/edit/drag-drop için merkezi izin kararları
- Kullanıcıya gösterilecek izin özeti

---

## 5.6 Takvim / görev planı

### `app/src/main/java/org/example/CalendarModuleView.java`
- Aylık takvim görünümü
- Seçili gün etkinlik listesi
- Görev planı sekmesi
- Çift tık ile görev planına geçiş
- Görev CRUD akışları
- Google Calendar bağlantı ve event yükleme UI akışı

### `app/src/main/java/org/example/TaskItem.java`
- Görev veri modeli.

### `app/src/main/java/org/example/TaskStorageService.java`
- Görevlerin diskten okunması/yazılması.

### `app/src/main/java/org/example/CalendarEventItem.java`
- Google Calendar’dan gelen etkinliklerin UI modeli.

### `app/src/main/java/org/example/GoogleCalendarService.java`
- Google Calendar verilerini çeken servis.
- Bağlanma durumu.
- Hata mesajı.
- Event yükleme.
- Ay bazlı event cache yönetimi.
- Bağlantı yokken event yükleme denemesini güvenli şekilde boş döndürme.

### `app/src/main/java/org/example/GoogleCalendarOAuthService.java`
- OAuth yetkilendirme katmanı.
- `LocalServerReceiver` tabanlı masaüstü callback akışı.
- Tokenları `config/google-calendar-tokens/` altında saklama.

---

## 5.7 Referans modülü

### `app/src/main/java/org/example/ReferenceLibraryView.java`
- Kategori listesi
- Konu listesi
- İçerik editörü
- Markdown tabanlı veri saklama
- Metadata + markdown içerik ayrımı

Aşama F’de zenginleştirilecek ana dosya budur.

---

## 5.8 UART

### `app/src/main/java/org/example/UartConsoleView.java`
- UART panel UI’si
- Port tarama butonu
- Port/baud seçimi
- Bağlan/kes akışı
- Log paneli
- TX gönderme alanı
- Satır sonu seçimi
- Timestamp ve auto-scroll seçenekleri

### `app/src/main/java/org/example/SerialPortService.java`
- Port tarama
- Bağlanma
- `stty` yapılandırması
- Seri veri okuma thread’i
- Veri gönderme
- Bağlantı kapatma
- Hata callback yönetimi

---

# 6. Örnek `config.json` Yapısı

```json
{
  "app": {
    "name": "Berkay Academic OS",
    "version": "1.5.0",
    "theme": "light"
  },
  "ui": {
    "sidebarWidth": 220,
    "defaultStatus": "Hazır",
    "webNotesEnabledByDefault": true
  },
  "storage": {
    "notesDir": "data/notes",
    "cacheDir": "data/cache",
    "downloadsDir": "data/downloads"
  },
  "integrations": {
    "googleDrive": {
      "enabled": true,
      "credentialsPath": "config/google-drive-credentials.json",
      "tokensPath": "config/google-drive-tokens",
      "rootFolderName": "BerkayAcademicNotes"
    }
  },
  "modules": [
    {
      "id": "documents",
      "name": "Belge Merkezi",
      "category": "Verimlilik",
      "type": "file_explorer",
      "enabled": true,
      "icon": "folder",
      "message": "Belge merkezi açıldı",
      "explorerOptions": {
        "defaultRoot": "/home/berkay/GoogleDriveMount",
        "readOnly": false,
        "allowCreateFolder": true,
        "allowCreateFile": true,
        "allowRename": true,
        "allowDelete": true,
        "allowDragDropImport": true,
        "showPreviewPane": true,
        "showHiddenFiles": false
      },
      "previewOptions": {
        "allowedExtensions": [".txt", ".md", ".json", ".csv", ".java", ".png", ".jpg", ".jpeg", ".webp", ".pdf"],
        "openExternallyExtensions": [".docx", ".xlsx", ".pptx"],
        "maxPreviewSizeBytes": 2097152,
        "maxImagePreviewSizeBytes": 10485760,
        "showFileMetadata": true,
        "previewTextFiles": true,
        "previewImageFiles": true,
        "openPdfExternally": true
      }
    },
    {
      "id": "wikipedia",
      "name": "Wikipedia",
      "category": "Akademik",
      "type": "web",
      "enabled": true,
      "icon": "globe",
      "message": "Wikipedia açıldı",
      "target": "https://www.wikipedia.org"
    },
    {
      "id": "reference",
      "name": "Referans",
      "category": "Akademik",
      "type": "internal",
      "enabled": true,
      "icon": "book",
      "message": "Referans modülü açıldı"
    },
    {
      "id": "uart",
      "name": "UART Konsolu",
      "category": "Embedded",
      "type": "internal",
      "enabled": true,
      "icon": "serial",
      "message": "UART monitör modülü açıldı"
    },
    {
      "id": "calendar",
      "name": "Takvim",
      "category": "Planlama",
      "type": "internal",
      "enabled": true,
      "icon": "calendar",
      "message": "Takvim modülü açıldı"
    }
  ]
}
```

---

# 7. Runtime Veri Dosyaları

## 7.1 Notlar

```text
data/notes/
```

## 7.2 Görevler

```text
data/tasks.json
```

## 7.3 Referans kütüphanesi

```text
data/reference-library/index.json
data/reference-library/topics/<kategori>/<dosya>.md
```

## 7.4 Google Calendar tokenları

```text
config/google-calendar-tokens/
```

## 7.5 Google Calendar credential dosyası

```text
config/google-calendar-credentials.json
```

## 7.6 Google Drive credential/token dosyaları

```text
config/google-drive-credentials.json
config/google-drive-tokens/
```

---

# 8. Karşılaşılan Büyük Sorunlar ve Ne Yapıldı

## 8.1 Config yüklenemedi sorunu

Sebep:
- Config alanları ile Java model sınıfları birebir uyuşmayabiliyordu.

Çözüm:
- `ConfigLoader` tarafında bilinmeyen alanları tolere edecek yapı önerildi.
- Hata mesajını status ve center panelde görünür yapma fikri eklendi.

---

## 8.2 TaskStorageService sorunu

Sebep:
- `JavaTimeModule` bağımlılığı veya `LocalDate` serileştirme uyumsuzluğu.

Çözüm:
- Daha dayanıklı okuma/yazma yaklaşımı.
- Null parent kontrolleri.
- Bağımlılık hassasiyetini azaltan tasarım.

---

## 8.3 ReferenceLibrary derleme hataları

Sebep:
- `ReferenceTopic`, `ReferenceStorageService` gibi ayrı sınıflar referans veriliyordu ama projede bulunmuyordu.

Çözüm:
- Referans modülünü tek başına çalışacak gömülü yardımcı sınıflarla yazmak.
- Metadata + markdown mantığını tek view içinde toparlamak.

---

## 8.4 PreviewService uyumsuzluğu

Sebep:
- `FileExplorerView`, `PreviewService` içinde belirli metotları bekliyordu.
- Preview API’si zamanla değiştiği için view-service uyumu bozulabiliyordu.

Çözüm:
- Aşama E’de karar mekanizması `decide(...)` ve `PreviewDecision` ile netleştirildi.
- `EDITABLE_TEXT`, `IMAGE`, `PDF`, `EXTERNAL`, `UNSUPPORTED` modları standartlaştırıldı.

---

## 8.5 Google OAuth girişte çökme

Sebep olası:
- OAuth akışının başlangıçta/yanlış anda tetiklenmesi.
- Local receiver / browser açma katmanının stabil olmaması.
- Auth akışının UI ile çok sıkı bağlı olması.

Çözüm:
- OAuth başlangıçtan çıkarıldı.
- Buton ile tetiklenen bağlanma akışına geçildi.
- OAuth `LocalServerReceiver` tabanlı callback yapısına alındı.
- Bağlanma ve event çekme arka plan `Task` yapısına taşındı.

---

## 8.6 Takvimde çift tık algısının bozulması

Sebep:
- İlk tıkta `refreshCalendarView()` çağrılıp butonlar yeniden oluşturuluyordu.

Çözüm:
- Seçili gün güncellemesi ayrı tutuldu.
- Header update ayrı tutuldu.
- Çift tıkta görev planı sekmesine geçiş düzeltildi.

---

## 8.7 Drag & Drop import izin ve çakışma riski

Sebep:
- Drag-drop import doğrudan dosya sistemi yazma işlemi yaptığı için `readOnly`, izin ve isim çakışması riskleri vardı.

Çözüm:
- UI ve servis katmanında `canDragDropImport()` kontrolü yapıldı.
- Aynı isimlerde overwrite yerine otomatik yeni ad üretildi.
- Klasör kopyalama recursive hale getirildi.
- Klasörün kendi içine kopyalanması engellendi.

---

## 8.8 UART port erişim ve UI donma riski

Sebep:
- Port tarama/bağlanma IO ağırlıklı işlemlerdi.
- Linux’ta `/dev/ttyUSB*` erişimi için kullanıcı grubu izni gerekebiliyordu.

Çözüm:
- Port tarama ve bağlanma UI thread dışına taşındı.
- `SerialPortService` daha sağlam hata mesajları üretir hale getirildi.
- `dialout` grubu uyarısı eklendi.

---

# 9. Şu Anda Tamamlanmış Başlıklar

Tamamlanan veya işlevsel olanlar:
- Config tabanlı modül yapısı
- Sidebar
- Dahili web + not sistemi
- Script modülü
- Belge merkezi temel işlevleri
- Klasör gezinme ve async yükleme
- Recursive delete
- Takvim görev planı
- Görev kalıcılığı
- Referans modülü veri dosyası mantığı
- Takvim sekmeli yapı
- Çift tıkla görev planına geçiş mantığı
- Google Calendar için OAuth tabanlı iskelet
- Google Calendar OAuth/Event yükleme stabilizasyonu
- Belge Merkezi `readOnly` ve işlem izinleri
- Belge Merkezi izin kontrollü servis metotları
- Belge Merkezi Drag & Drop import
- UART port tarama / bağlanma / veri gönderme-alma ürünleşmesi
- Preview metadata / görsel / PDF / büyük dosya davranışları

---

# 10. Bilerek Ertelenen / Tam Bitmeyen Alanlar

## 10.1 Belge Merkezi readOnly / yetki kontrolü

Durum:
- **Aşama B kapsamında tamamlandı.**

Kalan küçük takip notu:
- İleride global kaydetme kısayolu eklenirse aynı izin kontrolü o akışa da bağlanmalı.

---

## 10.2 Drag & Drop Import

Durum:
- **Aşama C kapsamında tamamlandı.**

Kalan küçük takip notları:
- Çok büyük klasör importlarında progress bar eklenebilir.
- Import geçmişi/log dosyası tutulabilir.
- Import sırasında iptal butonu eklenebilir.

---

## 10.3 UART gerçek seri haberleşme ürünleşmesi

Durum:
- **Aşama D kapsamında tamamlandı.**

Kalan küçük takip notları:
- Log’u `.txt` dosyasına kaydetme
- Hex görüntüleme modu
- Hex veri gönderme modu
- Gelen veride filtreleme / arama
- Otomatik reconnect
- Windows desteği için ileride `jSerialComm` benzeri kütüphaneye geçme

---

## 10.4 Preview polish

Durum:
- **Aşama E kapsamında tamamlandı.**

Kalan küçük takip notları:
- PDF için gerçek dahili sayfa renderer
- Markdown için render edilmiş preview modu
- Metin dosyalarında arama
- Syntax highlighting
- Image zoom in / zoom out butonları
- Dosya metadata panelini ayrı katlanabilir bölüm yapmak

---

## 10.5 Google Calendar entegrasyonu olgunlaştırma

Durum:
- **Aşama A kapsamında stabilizasyon tamamlandı.**

Kalan geliştirme notları:
- Disconnect/reconnect butonu ileride eklenebilir.
- İleride readonly dışı event oluşturma/güncelleme desteği ayrı aşama olarak ele alınabilir.

---

## 10.6 Referans modülü zenginleştirme

Durum:
- **Sıradaki aktif aşama: Aşama F.**

Yapılacaklar:
- Arama
- Etiket filtresi
- Kategori yönetimi
- İçerik içi arama
- Markdown preview
- Dış kaynak link alanı

---

# 11. Bundan Sonra Yapacağım İşler (Öncelik Sırasıyla)

## Aşama A — Takvim / Google Calendar stabilizasyonu

Durum:
- **Tamamlandı.**

İlgili dosyalar:
- `CalendarModuleView.java`
- `GoogleCalendarService.java`
- `GoogleCalendarOAuthService.java`
- `CalendarEventItem.java`

---

## Aşama B — Belge Merkezi yetki kontrolü

Durum:
- **Tamamlandı.**

İlgili dosyalar:
- `FileExplorerView.java`
- `FileOperationService.java`
- `FileExplorerOptions.java`
- `PreviewService.java`
- `config.json`

---

## Aşama C — Drag & Drop import

Durum:
- **Tamamlandı.**

Tamamlananlar:
1. Dışarıdan dosya sürükle bırak
2. Dışarıdan klasör sürükle bırak
3. Hedef klasöre kopyalama
4. Klasör için recursive copy
5. Aynı isimli dosya/klasör için çakışma yönetimi
6. Hata uyarıları
7. `allowDragDropImport` ve `readOnly` izinlerine tam uyum
8. Import sonrası klasör listesini yenileme

İlgili dosyalar:
- `FileExplorerView.java`
- `FileOperationService.java`
- `FileExplorerOptions.java`

---

## Aşama D — UART ürünleştirme

Durum:
- **Tamamlandı.**

Tamamlananlar:
1. Gerçek port yönetimi
2. Baud rate ayarı
3. Bağlanma durumu
4. Veri log paneli
5. Gönderme/alma stabilizasyonu
6. Hata yönetimi
7. Async port tarama ve bağlanma
8. Satır sonu seçimi
9. Timestamp ve auto-scroll

İlgili dosyalar:
- `UartConsoleView.java`
- `SerialPortService.java`

---

## Aşama E — Preview polish

Durum:
- **Tamamlandı.**

Tamamlananlar:
1. Görsel preview iyileştirmesi
2. PDF davranışı netleştirme
3. Büyük dosya davranışları
4. Config’ten gelen preview kurallarına uyum
5. Metadata / bilgi paneli görünümü
6. Desteklenmeyen dosya türleri için açıklayıcı kart
7. Harici açma akışının iyileştirilmesi

İlgili dosyalar:
- `FileExplorerView.java`
- `PreviewService.java`
- `PreviewOptions.java`

---

## Aşama F — Referans modülü zenginleştirme

Durum:
- **Sıradaki aktif aşama.**

Yapılacaklar:
1. Referans konularında arama
2. Etiket filtresi
3. Kategori oluşturma / yeniden adlandırma / silme
4. İçerik içi arama
5. Markdown preview
6. Dış kaynak link alanı
7. Konu metadata alanlarını zenginleştirme
8. Daha okunur split-pane arayüz

İlgili dosya:
- `ReferenceLibraryView.java`

---

## Aşama G — Ek ürünleştirme hattı

Aşama F sonrası düşünülebilecek işler:
- Global arama
- Ayarlar ekranı
- Tema / koyu mod
- Veritabanı veya index tabanlı arama
- Google Drive ile gerçek senkronizasyon
- Belge Merkezi import geçmişi
- UART log kaydetme
- Uygulama içi hata raporu paneli

---

# 12. En Kısa Haliyle Projenin Şu Anki Durumu

Şu anda proje:
- basit JavaFX denemesi olmaktan çıktı
- modüler çalışan bir masaüstü uygulama iskeleti haline geldi
- belge merkezi, takvim/görev planı, referans modülü, UART konsolu ve preview sistemi gerçek iş yapan araçlara dönüştü
- Google Calendar entegrasyonunda Aşama A stabilizasyonu tamamlandı
- Belge Merkezi readOnly/yetki kontrolünde Aşama B tamamlandı
- Belge Merkezi Drag & Drop import için Aşama C tamamlandı
- UART ürünleşmesi için Aşama D tamamlandı
- Preview polish için Aşama E tamamlandı
- sıradaki ana odak Referans modülünün zenginleştirilmesi oldu

Kalan en kritik ana başlıklar:
1. Referans modülü zenginleştirme
2. UART gelişmiş log/hex özellikleri
3. PDF için gerçek dahili renderer
4. Markdown preview
5. Google Calendar disconnect/reconnect ve ileride yazma desteği
6. Global ayarlar / tema / arama altyapısı

---

# 13. Dosya Bazlı Hızlı Özet

## Çekirdek
- `App.java` → uygulama başlangıcı
- `ConfigLoader.java` → config yükleme
- `AppConfig.java` → config kök modeli

## Modül yönlendirme
- `SidebarBuilder.java`
- `ModuleActionHandler.java`

## Web
- `WebModuleView.java`

## Script
- `ScriptModuleRunner.java`

## Belge Merkezi
- `FileExplorerView.java`
- `FileOperationService.java`
- `PreviewService.java`
- `PreviewOptions.java`
- `ExplorerItem.java`
- `FavoriteLocation.java`
- `FileExplorerOptions.java`

## Takvim/Görev
- `CalendarModuleView.java`
- `TaskItem.java`
- `TaskStorageService.java`

## Google Calendar
- `CalendarEventItem.java`
- `GoogleCalendarService.java`
- `GoogleCalendarOAuthService.java`

## Referans
- `ReferenceLibraryView.java`

## UART
- `UartConsoleView.java`
- `SerialPortService.java`

---

# 14. Aşama C/D/E Dosya Yerleştirme Notları

Aşama C sonrası kullanılan dosyalar:
- `FileExplorerView_AşamaC.java` → daha sonra Aşama E ile güncellendiği için nihai dosya olarak kullanılmamalı.
- `FileOperationService_AşamaC.java` → `FileOperationService.java` olarak kullanılmalı.

Aşama D sonrası kullanılan dosyalar:
- `UartConsoleView_AsamaD.java` → `UartConsoleView.java` olarak kullanılmalı.
- `SerialPortService_AsamaD.java` → `SerialPortService.java` olarak kullanılmalı.

Aşama E sonrası kullanılan dosyalar:
- `FileExplorerView_AsamaE.java` → `FileExplorerView.java` olarak kullanılmalı.
- `PreviewOptions_AsamaE.java` → `PreviewOptions.java` olarak kullanılmalı.
- `PreviewService_AsamaE.java` → `PreviewService.java` olarak kullanılmalı.

Önemli not:
- Aşama E’de `FileExplorerView.java` yeniden güncellendiği için Belge Merkezi tarafında nihai view dosyası Aşama E sürümüdür.
- Aşama C’de eklenen drag-drop import davranışı Aşama E sürümünde korunmalıdır.
- `FileOperationService.java` için Aşama C sürümündeki import metotları korunmalıdır.
- UART için Aşama D sürümleri nihai UART dosyalarıdır.

---

# 15. Son Not

Bu doküman, şu ana kadar yapılan ve bundan sonra yapılacak işleri topluca teknik referans olarak tutmak için baştan düzenlendi.

Şu anda en aktif odak:

## **Aşama F — Referans modülü zenginleştirme**

Tamamlanan son ana aşamalar:
- **Aşama A:** Takvim + Google Calendar stabilizasyonu
- **Aşama B:** Belge Merkezi readOnly/yetki kontrolü
- **Aşama C:** Belge Merkezi Drag & Drop import
- **Aşama D:** UART ürünleştirme
- **Aşama E:** Preview polish

Bundan sonraki ana ürünleştirme hattı:

**Referans modülü zenginleştirme + Markdown preview + global arama + gelişmiş ayarlar.**
