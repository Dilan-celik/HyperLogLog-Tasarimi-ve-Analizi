import java.nio.charset.StandardCharsets;

public class FNV1aMurmurHash implements IHashFunction {
    @Override
    public long hash(String data) {
        long hash = 0xcbf29ce484222325L; // FNV offset basis
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        // FNV-1a Algoritması
        for (byte b : bytes) {
            hash ^= (b & 0xff);
            hash *= 0x100000001b3L; // FNV prime
        }

        // MurmurHash3 Avalanche (Çığ) Etkisi - Bitlerin homojen dağılımı için
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= hash >>> 33;

        return hash;
    }
}