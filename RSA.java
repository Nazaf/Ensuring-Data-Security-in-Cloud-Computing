package Server;

class RSA
{
    int p, q, n, z, d, e;

    RSA()
    {
        p = 13;
        q = 17;
        n = p * q;
        z = (p - 1) * (q - 1);
        d = 19;

        for(e = 1; e < z; ++e)
        {
            if((( e * d) % z) == 1)
                break;
        }
    }

    int gcd(int  x,int y)
    {
        while(x != y)
        {
            if(x > y)
                x = (short)(x - y);
            else
                y = (short)(y - x);
        }

        return x;
    }

    int modexp(int x, int y, int n)
    {
        int k = 1, i;

        for(i = 0; i < y; i++)
        k = (k * x) % n;

        return k;
    }

    String encrypt(String msg)
    {
        int i;
        char[] cmsg = new char[msg.length() + 1];
        char[] emsg = new char[msg.length() + 1];
        int[] imsg = new int[msg.length() + 1];

        msg.getChars(0, msg.length(), cmsg, 0);

        for(i = 0; i < msg.length(); i++)
            imsg[i] = cmsg[i];

        for(i = 0; i < msg.length(); i++)
            emsg[i] = (char)modexp(imsg[i], e, n);

        System.out.println("Encrypted string is : " + new String(emsg, 0, msg.length()));

        return msg;
    }

    String decrypt(String msg)
    {
        int i;
        char[] cmsg = new char[msg.length() + 1];
        char[] dmsg = new char[msg.length() + 1];
        int[] imsg = new int[msg.length() + 1];

        msg.getChars(0, msg.length(), cmsg, 0);

        for(i = 0; i < msg.length(); i++)
            imsg[i] = cmsg[i];

        for(i = 0; i < msg.length(); i++)
            dmsg[i] = (char)modexp(imsg[i], d, n);

        System.out.println("Decrypted string is : " + msg);

        return msg;
    }

    public static void main(String argv[]) throws Exception
    {
        RSA rsa = new RSA();
        String emsg = rsa.encrypt("LOGIN|admin|password");
        String dmsg = rsa.decrypt(emsg);
        System.out.println(emsg);
        System.out.println(dmsg);
    }
}