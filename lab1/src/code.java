public class code {
    public static void main(String[] args) {

        long[] c = new long[11];

        int num = 21;

        for (int i = 0; i < 11; i++) {
            c[i] = num;
            num -= 2;
        }

        for (int i = 0; i < 11; i++) {
            System.out.print(c[i] + " ");
        }

        float[] x = new float[20];

        for (int i = 0; i < x.length; i++) {
            float randomNumber = (float) (Math.random() * (12.0 - (-15.0)) - 15.0);
            x[i] = randomNumber;

        }
        int gSizeI = 11;
        int gSizeJ = 20;
        double[][] g = new double[gSizeI][gSizeJ];

        for (int i = 0; i < gSizeI; i++) {
            for (int j = 0; j < gSizeJ; j++) {
                if (c[i] == 15) {
                    g[i][j] = Math.pow(Math.pow(Math.exp(x[i]) / 2, 2), Math.sin(x[i]) * ((Math.pow(x[i], (x[i] / (x[i] + 0.5))) + 1) / 2));
                } else if (c[i] == 3 || c[i] == 5 || c[i] == 17 || c[i] == 19 || c[i] == 21) {
                    g[i][j] = Math.pow(4 + Math.sqrt(Math.pow(x[i], 2 / (1 - x[i]))), 3) / (0.5 * Math.log10(Math.abs(x[i]))) * Math.log10(Math.pow(Math.abs(x[i]) / (3 + Math.abs(x[i])), 2));
                } else {
                    g[i][j] = Math.atan(Math.exp(Math.sqrt(Math.pow((-1) * Math.sin(x[i]), 2)) / 2));
                }
            }
        }

        for (int i = 0; i < gSizeI; i++) {
            for (int j = 0; j < gSizeJ; j++) {
                System.out.printf("%.4f ", g[i][j]);
            }
            System.out.println();
        }
    }
}
