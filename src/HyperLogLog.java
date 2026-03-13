public class HyperLogLog {
    private final int b;
    private final int m;
    private final double alphaMM;
    private final byte[] registers;
    private final IHashFunction hashFunction;

    public HyperLogLog(int b, IHashFunction hashFunction) {
        if (b < 4 || b > 16) {
            throw new IllegalArgumentException("b değeri 4 ile 16 arasında olmalıdır.");
        }
        this.b = b;
        this.m = 1 << b; // 2^b
        this.registers = new byte[m];
        this.hashFunction = hashFunction;
        this.alphaMM = calculateAlphaMM(m);
    }

    private double calculateAlphaMM(int m) {
        double alpha;
        switch (m) {
            case 16: alpha = 0.673; break;
            case 32: alpha = 0.697; break;
            case 64: alpha = 0.709; break;
            default: alpha = 0.7213 / (1.0 + 1.079 / m); break;
        }
        return alpha * m * m;
    }

    public void add(String element) {
        long hashValue = hashFunction.hash(element);
        int bucketIndex = (int) (hashValue >>> (64 - b));
        long remainingBits = hashValue << b;
        int w = Long.numberOfLeadingZeros(remainingBits) + 1;

        if (w > registers[bucketIndex]) {
            registers[bucketIndex] = (byte) w;
        }
    }

    // Detaylı analiz için tek bir elemanın nasıl işlendiğini döndüren metod
    public String analyzeElement(String element) {
        long hashValue = hashFunction.hash(element);
        String binaryString = String.format("%64s", Long.toBinaryString(hashValue)).replace(' ', '0');

        String bucketBits = binaryString.substring(0, b);
        String remainingBitsStr = binaryString.substring(b);

        int bucketIndex = (int) (hashValue >>> (64 - b));
        long remainingBits = hashValue << b;
        int w = Long.numberOfLeadingZeros(remainingBits) + 1;

        return String.format("Veri: '%s'\n" +
                        "Hash (Binary): %s | %s\n" +
                        "Kova İndeksi (İlk %d bit): %d\n" +
                        "Ardışık Sıfır Sayısı + 1 (w): %d\n" +
                        "--------------------------------------------------",
                element, bucketBits, remainingBitsStr, b, bucketIndex, w);
    }

    public long count() {
        double harmonicMeanSum = 0;
        int emptyRegisters = 0;

        for (int i = 0; i < m; i++) {
            harmonicMeanSum += Math.pow(2.0, -registers[i]);
            if (registers[i] == 0) {
                emptyRegisters++;
            }
        }

        double estimate = alphaMM / harmonicMeanSum;

        // Linear Counting Correction (Küçük Veriler)
        if (estimate <= 2.5 * m) {
            if (emptyRegisters > 0) {
                estimate = m * Math.log((double) m / emptyRegisters);
            }
        }
        return Math.round(estimate);
    }

    public HyperLogLog merge(HyperLogLog other) {
        if (this.b != other.b) {
            throw new IllegalArgumentException("HLL b değerleri eşleşmiyor!");
        }
        HyperLogLog merged = new HyperLogLog(this.b, this.hashFunction);
        for (int i = 0; i < this.m; i++) {
            merged.registers[i] = (byte) Math.max(this.registers[i], other.registers[i]);
        }
        return merged;
    }

    public double getTheoreticalError() {
        return (1.04 / Math.sqrt(m)) * 100;
    }

    public int getBucketCount() { return m; }
}
