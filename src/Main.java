import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   HYPERLOGLOG (HLL) BÜYÜK VERİ ANALİTİĞİ MOTORU  ");
        System.out.println("==================================================\n");

        IHashFunction hashAlgorithm = new FNV1aMurmurHash();
        int b = 12; // 12 bit -> 4096 Kova
        HyperLogLog hll = new HyperLogLog(b, hashAlgorithm);

        System.out.printf("[BİLGİ] Kova Bit Sayısı (b): %d\n", b);
        System.out.printf("[BİLGİ] Kova Sayısı (m): %d\n", hll.getBucketCount());
        System.out.printf("[BİLGİ] Teorik Hata Sınırı: +/- %%%.3f\n\n", hll.getTheoreticalError());

        System.out.println(">>> 1. ADIM: BIT SEVİYESİNDE İŞLEM ANALİZİ (Örnek Veriler) <<<");
        System.out.println(hll.analyzeElement("veri_madenciligi"));
        System.out.println(hll.analyzeElement("hyperloglog_algoritmasi"));
        System.out.println(hll.analyzeElement("buyuk_veri_analitigi"));

        System.out.println("\n>>> 2. ADIM: KÜÇÜK VERİ SETİ SİMÜLASYONU (Linear Counting Devrede) <<<");
        Set<String> controlSet = new HashSet<>();
        for (int i = 1; i <= 500; i++) {
            String data = "user_id_" + i;
            hll.add(data);
            hll.add(data); // Tekrarlı veri atıyoruz ki Unique count'un çalıştığı görülsün
            controlSet.add(data);
        }
        System.out.println("HashSet ile Bulunan Gerçek Benzersiz Veri: " + controlSet.size());
        System.out.println("HyperLogLog Tahmini: " + hll.count());

        System.out.println("\n>>> 3. ADIM: BÜYÜK VERİ SETİ SİMÜLASYONU (Harmonic Mean Devrede) <<<");
        HyperLogLog largeHll = new HyperLogLog(b, hashAlgorithm);
        int largeScale = 1_000_000; // 1 Milyon Veri
        System.out.println("1.000.000 adet benzersiz veri işleniyor... (Lütfen bekleyiniz)");
        for (int i = 0; i < largeScale; i++) {
            largeHll.add("global_user_" + i);
        }
        long estimatedCount = largeHll.count();
        double errorMargin = Math.abs(largeScale - estimatedCount) / (double) largeScale * 100;

        System.out.println("Gerçek Benzersiz Veri Miktarı : 1000000");
        System.out.println("HyperLogLog Tahmini           : " + estimatedCount);
        System.out.printf("Gerçekleşen Hata Oranı        : %%%.3f\n", errorMargin);

        System.out.println("\n>>> 4. ADIM: DAĞITIK SİSTEMLERDE BİRLEŞTİRME (MERGE) TESTİ <<<");
        HyperLogLog hllNode1 = new HyperLogLog(b, hashAlgorithm);
        HyperLogLog hllNode2 = new HyperLogLog(b, hashAlgorithm);

        // Node 1'e 50 bin veri
        for(int i=0; i<50000; i++) hllNode1.add("node1_data_" + i);

        // Node 2'ye 75 bin veri (İlk 10 bini Node1 ile aynı, çakışma testi)
        for(int i=40000; i<115000; i++) hllNode2.add("node1_data_" + i);

        HyperLogLog mergedCluster = hllNode1.merge(hllNode2);
        long mergedTotal = mergedCluster.count();

        System.out.println("Node 1 İçerik   : 50.000 veri");
        System.out.println("Node 2 İçerik   : 75.000 veri (10.000 tanesi Node1 ile ortak)");
        System.out.println("Beklenen Toplam : 115.000 benzersiz veri");
        System.out.println("Merged HLL Tahmini: " + mergedTotal);
        System.out.printf("Birleştirme Hata Oranı: %%%.3f\n", Math.abs(115000 - mergedTotal) / 115000.0 * 100);

        System.out.println("==================================================");
        System.out.println("            SİMÜLASYON TAMAMLANDI                 ");
        System.out.println("==================================================");
    }
}