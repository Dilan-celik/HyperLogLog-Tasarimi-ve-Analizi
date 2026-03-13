public interface IHashFunction {
    /**
     * Gelen metin verisini 64-bitlik (long) bir hash değerine dönüştürür.
     */
    long hash(String data);
}