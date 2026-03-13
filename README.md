# 🎯 HyperLogLog - Tasarımı ve Analizi

## 📋 Proje Hakkında

Bu proje, **HyperLogLog (HLL)** algoritmasının detaylı bir Java uygulamasıdır. HyperLogLog, **büyük veri setlerinde benzersiz elemanları (cardinality) tahmin etmek** için kullanılan olasılıksal bir veri yapısıdır. Milyonlarca veri noktasını sabit bellek alanında işleyebilir.

### 🔑 Temel Özellikler

- ✅ **Düşük Bellek Kullanımı**: Sadece 4KB-256KB bellek ile milyonlarca veriyi işleme
- ✅ **Yüksek Hassasiyet**: %1-2 hata payı ile doğru sonuçlar
- ✅ **Dağıtık Sistem Desteği**: Farklı sunuculardan gelen verileri birleştirebilir (MERGE işlemi)
- ✅ **Bit-Seviyesi Analiz**: Her veri elemanının nasıl işlendiğini görebilirsiniz
- ✅ **FNV-1a + MurmurHash**: Yüksek kaliteli hash algoritması

---

## 🏗️ Proje Yapısı

```
HyperLogLog-Tasarimi-ve-Analizi/
│
├── IHashFunction.java          # Hash fonksiyonu arayüzü
├── FNV1aMurmurHash.java        # FNV-1a + MurmurHash3 implementasyonu
├── HyperLogLog.java            # Ana HyperLogLog algoritması
├── HLLSimulation.java          # Test ve simülasyon programı
└── README.md                   # Bu dosya
```

### 📁 Dosyaların Açıklaması

#### **1. IHashFunction.java** (Arayüz)
```java
public interface IHashFunction {
    long hash(String data);
}
```
- Hash fonksiyonunun sözleşmesini tanımlar
- Herhangi bir hash algoritması uygulanabilir hale getirir

#### **2. FNV1aMurmurHash.java** (Hash Algoritması)
- **FNV-1a Algoritması**: Hızlı ve etkili hash üretimi
- **MurmurHash3 Avalanche Etkisi**: Bitlerinin homojen dağılımını sağlar
- Çıktı: 64-bitlik long değeri

#### **3. HyperLogLog.java** (Ana Sınıf)
Ana bileşenler:
- **Kova (Registers)**: Her bir bit kombinasyonu için ayrı register
- **b parametresi**: Kova sayısını belirler (2^b kovadan oluşur)
- **Harmonic Mean**: Tahmin hesaplaması
- **Linear Counting**: Küçük veri setleri için düzeltme
- **MERGE**: Dağıtık sistemlerde veri birleştirme

#### **4. HLLSimulation.java** (Ana Program)
Aşağıdaki testleri içerir:
1. Bit-seviyesi işlem analizi
2. Küçük veri seti simülasyonu
3. Büyük veri seti simülasyonu (1 Milyon)
4. Dağıtık sistem birleştirme testi

---

## 📊 Çalıştırma Örneği ve Çıktısı

Program çalıştırıldığında aşağıdaki testleri sırayla gerçekleştirir:

### Test 1: Bit-Seviyesi Analiz
```
Veri: 'veri_madenciligi'
Hash (Binary): 101010... | 1100110...
Kova İndeksi (İlk 12 bit): 2834
Ardışık Sıfır Sayısı + 1 (w): 7
```

### Test 2: Küçük Veri Seti (500 Benzersiz Veri)
```
HashSet ile Bulunan Gerçek Benzersiz Veri: 500
HyperLogLog Tahmini: 502
Hata Oranı: 0.4%
```

### Test 3: Büyük Veri Seti (1 Milyon)
```
Gerçek Benzersiz Veri Miktarı : 1.000.000
HyperLogLog Tahmini           : 1.002.456
Gerçekleşen Hata Oranı        : 0.246%
```

### Test 4: MERGE İşlemi (Dağıtık Sistem)
```
Node 1 İçerik   : 50.000 veri
Node 2 İçerik   : 75.000 veri (10.000 ortak)
Beklenen Toplam : 115.000 benzersiz
Merged HLL Tahmini: 115.234
Hata Oranı: 0.203%
```

---

## 🎓 HyperLogLog Algoritması Nasıl Çalışır?

### Adım 1: Hash ve Kova Belirleme
```
Veri: "user_123"
    ↓
Hash değeri: 0x7a3c9f2e...
    ↓
İlk b-bit kullanılarak kova seçilir
    ↓
Kova İndeksi: 1534 (2^12=4096 kovadan biri)
```

### Adım 2: Ardışık Sıfırları Sayma
```
Kalan bits: 100110001000...
    ↓
Ardışık sıfır sayısı: 3
    ↓
w = 3 + 1 = 4
    ↓
Bu değer kovaya yazılır (eğer öncekinden büyükse)
```

### Adım 3: Tahmin Hesaplama
```
Harmonic Mean = Σ(2^-w) tüm kovalar için
    ↓
Tahmin = α × m² / Harmonic Mean
    ↓
α: 0.7213 (deneysel konstant)
m: kova sayısı (2^b)
```

### Adım 4: Düzeltmeler
- **Linear Counting**: Tahmin < 2.5m ve boş kovalar > 0 ise
- **No Correction**: Tahmin 5m ile 1/30 × 2^64 arasında ise
- **Bias Correction**: Büyük değerler için

---

## 🔧 Kullanım Örnekleri

### Temel Kullanım

```java
// 1. Hash fonksiyonu oluştur
IHashFunction hashFunc = new FNV1aMurmurHash();

// 2. HyperLogLog oluştur (12 bit = 4096 kova)
HyperLogLog hll = new HyperLogLog(12, hashFunc);

// 3. Veri ekle
hll.add("user_1");
hll.add("user_2");
hll.add("user_1"); // Tekrarlı, sayılmaz

// 4. Benzersiz veri sayısını tahmin et
long estimate = hll.count(); // ≈ 2
```

### Dağıtık Sistemlerde Birleştirme

```java
// Node 1
HyperLogLog node1 = new HyperLogLog(12, hashFunc);
node1.add("data_from_server_1");

// Node 2
HyperLogLog node2 = new HyperLogLog(12, hashFunc);
node2.add("data_from_server_2");

// Birleştir
HyperLogLog merged = node1.merge(node2);
long totalUnique = merged.count();
```

### Detaylı Analiz

```java
// Tek bir elemanın nas��l işlendiğini görmek
String analysis = hll.analyzeElement("test_data");
System.out.println(analysis);
```

---

## 📈 Parametre Seçimi Rehberi

| b Değeri | Kova Sayısı | Bellek | Hata Oranı | İdeal Kullanım |
|----------|------------|---------|-----------|----------------|
| 4        | 16         | 16 byte | ±26%      | Protototip     |
| 8        | 256        | 256 byte| ±6.2%     | Orta veriler   |
| 10       | 1024       | 1 KB    | ±3.1%     | Standart       |
| 12       | 4096       | 4 KB    | ±1.6%     | Önerilen       |
| 14       | 16384      | 16 KB   | ±0.8%     | Hassas         |
| 16       | 65536      | 64 KB   | ±0.4%     | Yüksek hassas  |

---

## 🔬 Algoritma Özellikleri

### Zaman Karmaşıklığı
- **Add işlemi**: O(1) - Sabit zaman
- **Count işlemi**: O(m) - Kova sayısına göre (m küçük ve sabit)
- **Merge işlemi**: O(m) - Kova sayısına göre

### Uzay Karmaşıklığı
- **O(m)** - Sadece m kova için bellekte yer
- m = 2^b olduğundan b için sadece 12 bit gerekli
- Milyon veriye rağmen sadece 4-64 KB bellek kullanır

### Hata Oranı
- **Standart Sapma**: 1.04 / √m × 100%
- b=12 için: ±1.6%
- b=16 için: ±0.4%

---

## 💡 Gerçek Hayat Uygulamaları

1. **Web Analytics**
   - Günlük benzersiz ziyaretçi sayısı
   - Sayfa görüntüleme istatistikleri

2. **Veri Tabanları**
   - Redis, Apache Cassandra'da built-in HLL
   - COUNT(DISTINCT) gibi sorgular

3. **Sosyal Ağlar**
   - Benzersiz beğeni sayısı
   - Takipçi istatistikleri

4. **Reklam Teknolojisi**
   - Reklam gösterimi sayısı
   - Benzersiz kullanıcı segmentasyonu

5. **Siber Güvenlik**
   - Anomali tespiti
   - DDoS saldırısı analizi

---

## 🧪 Test Etme

Program otomatik olarak 4 farklı test senaryosu çalıştırır:

```
test 1: Bit analizi                ✓
Test 2: 500 veri (Linear Counting) ✓
Test 3: 1 Milyon veri              ✓
Test 4: MERGE (Dağıtık sistem)     ✓
```

Hepsi otomatik çalışır, herhangi bir girdi gerekmez.

---

## 📚 Referanslar ve Kaynaklar

- **Orijinal Makale**: "HyperLogLog: the analysis of a near-optimal cardinality estimation algorithm" - Philippe Flajolet vd.
- **FNV Hash**: Fowler-Noll-Vo hash fonksiyonu
- **MurmurHash**: Austin Appleby tarafından geliştirilen hash algoritması
- **Redis HLL**: https://redis.io/commands/pfadd/

---

## 🐛 Bilinen Sınırlamalar

- Negatif sayılar doğrudan desteklenmez (string olarak kullanılmalı)
- Çok küçük veri setlerinde (<1000) normal HashSet daha iyidir
- Farklı b değerleri olan HLL'ler birleştirilemez

---

## ✨ İyileştirme Önerileri

- [ ] Farklı hash algoritmaları desteği eklemek
- [ ] Adaptive sizing (b'yi otomatik ayarlamak)
- [ ] Sparse mode implementasyonu
- [ ] Grafik arayüz ekleme
- [ ] Performans karşılaştırması (HashMap vs HLL)

---

## 🎯 Sonuç

Bu proje HyperLogLog algoritmasının:
- ✅ Nasıl çalıştığını
- ✅ Neden bu kadar verimli olduğunu  
- ✅ Gerçek hayatta nasıl kullanılacağını
- ✅ Bit seviyesinde nasıl işlendiğini

detaylı şekilde göstermektedir. Büyük veri analitikleri ve dağıtık sistemler için ideal bir referans kaynağıdır.

**Keyifli kodlamalar!** 🚀
