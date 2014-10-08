package Server;

class Test
{
    public static void main(String argv[]) throws Exception
    {
        RSA rsa = new RSA();
        String emsg = rsa.encrypt("LOGIN|admin|admin");
        emsg = emsg + "\n";
        System.out.println(emsg);
        System.out.println(rsa.decrypt(emsg));
    }
}