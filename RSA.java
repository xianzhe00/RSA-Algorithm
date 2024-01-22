import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;


class RSA{

    public static void main(String[] args) throws IOException {
        String S;
        Scanner sc = new Scanner(System.in);
        System.out.print("\nEnter filename: ");
        String filename = sc.nextLine();
        System.out.println("Reading content of files and stored inside a String");
        S = Readfile(filename);
        //System.out.println(S);

        BigInteger[] keyS = keyScheduling();

        System.out.println("\nConverting every characters of the String to Ascii value....\n");
        String [] fileContent = convertStringToAscii(S);
        
        System.out.println("Performing encryption...\n");
        String[] encrypted_Data = encrypt(fileContent, keyS[0], keyS[1]);

        System.out.println("Done encrypted. Converting into hexadecimal characters and write to file(encryptedContent.txt)\n");
        String[] encrypted_Data_Hex = convertDecimalToHex(encrypted_Data);

        //Writing the hexadecimal characters to the encrypted.txt file
        String writeToFilename = "encryptedContent.txt";
        for(int i = 0; i < encrypted_Data_Hex.length; i++){
            writeFile(encrypted_Data_Hex[i], writeToFilename);
        }
        System.out.println("File writting done.\n");
        
        System.out.println("Reading he encrypted content from the file and stored inside a String\n");
        //Read the hexadecimal characters from the encrypted.txt file
        String[] readEncryptedFile = Readfile("encryptedContent.txt").split("\n");
  
        System.out.println("Converting hexadecimal characters into decimal and perform decryption\n");
        String[] decimal_Data = convertHexToDecimal(readEncryptedFile);
 
        String[] decrypted_String = decrypt(decimal_Data, keyS[0], keyS[2]);

        System.out.println("Converting Ascii value into characters and write to file(decryptedContent.txt)\n");
        String decryString = asciiToString(decrypted_String);
        //System.out.println(finalstr);                                     //(Debug Purpose) Check whether the output is correct or incorrect
        String writeToFilename2 = "decryptedContent.txt";
        writeFile(decryString, writeToFilename2);
        System.out.println("Encrypted content successfuly recovered.\n");
        System.out.println("Please check out both (encryptedContent.txt) and (decryptedContent.txt).\n");
    }

    
    
    public static BigInteger[] keyScheduling() {
        BigInteger[] keyS = new BigInteger[3];
        
        BigInteger p = generatePrime();
        BigInteger q = generatePrime();

        System.out.println("Testing the accuracy of the prime number using BigInteger.IsProbablePrime()");
        if(p.isProbablePrime(100)){
            System.out.println("p is a prime number");
        }

        if(q.isProbablePrime(100)){
            System.out.println("q is a prime number");
        }

        BigInteger N = p.multiply(q);

        BigInteger phiN = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        BigInteger E = BigInteger.valueOf(257);
 
        BigInteger x = modIvs(E, phiN);

        BigInteger d = x.mod(phiN);

        System.out.println("\nKeys generated");
        for(int i = 0; i < keyS.length;i++){
            if(i == 0){
                keyS[0] = N;
                System.out.println("n is: " + N);
            }
            else if(i == 1){
                keyS[1] = E;
                System.out.println("e is: " + E);
            }else if(i == 2){
                keyS[2] = d;
                System.out.println("d is: " + d);
            }
        }
        return keyS;
    }

    //Generating prime number which is larger than 2^64, (18446744073709551616)
    public static BigInteger generatePrime(){
        //generate 20 digits number 
        Random rand = new SecureRandom();
        byte[] bytes = new byte[9];
        bytes[0] = 1;
        BigInteger baseNum = new BigInteger(bytes);
        int counter = 0;
        do{         
            BigInteger addition = new BigInteger(32, rand);     //Random numbers are generated and added to the base number
            baseNum = baseNum.add(addition);                            //to make sure the value is more 18446744073709551616               
            if(isPrime(baseNum)){                                       //If a prime is generated, it will exit the loop with the condition which is counter not equal to 0
                counter++;  
                System.out.println(" ");
            }else{
                baseNum = baseNum.subtract(addition); 
                //System.out.println("Back to normal baseNum");         //(For debug purpose) Reset the value to generate another probably prime
                //System.out.println(baseNum);
            }
        }while(counter == 0);

        return baseNum;
    }

    //Reference: https://rosettacode.org/wiki/Miller%E2%80%93Rabin_primality_test
    //Miller–Rabin primality test
    //By referencing to https://oeis.org/A014233, using {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37} to test number n < 18,446,744,073,709,551,616 = 2^64 is sufficient
    public static boolean isPrime(BigInteger input){
        boolean result = true;
        int[] primesToTest = new int[] {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37};            
        BigInteger d = input.subtract(BigInteger.ONE);
        BigInteger s = BigInteger.ZERO;
        while(d.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)){
            d = d.shiftRight(1);
            s = s.add(BigInteger.ONE);
        }

        for(int num : primesToTest){
            if(try_composite(num,d,input,s)){
                result = false;
            }
        }


        return result;
    }

    private static boolean try_composite(int a, BigInteger d, BigInteger n, BigInteger s) {
        boolean result = true;
        BigInteger aB = BigInteger.valueOf(a);
        if (aB.modPow(d, n).equals(BigInteger.ONE)) {
            result = false;
        }
        for (int i = 0; BigInteger.valueOf(i).compareTo(s) < 0; i++) {
            // if pow(a, 2**i * d, n) == n-1
            if (aB.modPow(BigInteger.valueOf(2).pow(i).multiply(d), n).equals(n.subtract(BigInteger.ONE))) {
                result = false;
            }
        }
        return result;
    }

    //Reference: Method 2 from https://www.geeksforgeeks.org/multiplicative-inverse-under-modulo-m/
    //Modified the algorithm by Changing variable of Integer to BigInteger
    //Returns modulo inverse of a with respect to b using extended Euclid
    //Algorithm Assumption: a and b are coprimes, i.e., gcd(a, b) = 1
    public static BigInteger modIvs(BigInteger a, BigInteger b)
    {
        int counter = 0;
        BigInteger result = BigInteger.ZERO;
        BigInteger initial = b;
        BigInteger y = BigInteger.ZERO;
        BigInteger x = BigInteger.ONE;


        if (b.equals(BigInteger.ONE)){
            result = BigInteger.ZERO;
            counter++;
        }
            
        if(counter == 0){
            while (a.compareTo(BigInteger.ONE) > 0)
            {
                // q is quotient
                BigInteger q = a.divide(b);
                BigInteger t = b;
    
                // m is remainder now, process
                // same as Euclid's algo
                b = a.mod(b);
                a = t;
                t = y;
    
                // Update x and y
                y = x.subtract(q.multiply(y));
                x = t;
            }
    
            // Make x positive
            if (x.compareTo(BigInteger.ZERO) < 0){
                x = x.add(initial);
            }
            result = x;    
        }
        

        return result;
    }

    /**Reference: Lecture 6 Curtin University**/
    //BinaryModularExponentiation Function
    //BigInteger H is the Exponent 
    //BigInteger x is the base element
    //BigInteger n is the modulus
    public static BigInteger BME(BigInteger H, BigInteger x, BigInteger n) {
        BigInteger result = x;
        char[] binaryExp;
        binaryExp = H.toString(2).toCharArray();

        for (int i = 1 ; i < binaryExp.length ; i++) {
            result = result.pow(2).mod(n);
            if(binaryExp[i] == '1') {
                BigInteger temp = result.multiply(x);
                result = temp.mod(n);
            }
        }

        return result;
    }

    //To perform encryption of characters 1 by 1, then stored it in a String array
    //keyN is the n
    //KeyE is the e
    public static String[] encrypt(String[] input_AsciiString, BigInteger keyN, BigInteger keyE) {
        int x = input_AsciiString.length;
        String[] result = new String[x];

        for (int i = 0; i < x; i ++) {
            String str = input_AsciiString[i];
            BigInteger character = new BigInteger(str);
            BigInteger encryptedCharacter = BME(keyE, character, keyN);
            result[i] = (String)encryptedCharacter.toString();
        }

        return result;
    }

    //To perform decryption of characters 1 by 1, then stored it in a String array
    //keyN is the n
    //KeyE is the e
    public static String[] decrypt(String[] input_DecimalString, BigInteger keyN, BigInteger keyD) {
        int x = input_DecimalString.length;
        String[] result = new String[x];

        for (int i = 0; i < x; i ++) {
            String str = input_DecimalString[i];
            BigInteger character = new BigInteger(str);
            BigInteger decryptedCharacter = BME(keyD, character, keyN);
            result[i] = decryptedCharacter.toString();
        }

        return result;
    }

    //Readfile Function
    public static String Readfile(String fileName) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    //WriteFile Function
    public static void writeFile(String input, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
        writer.append(input);
        writer.append("\n");
        
        writer.close();
    }

    //Convert Ascii value to characters and stored it in a String Array 
    public static String asciiToString(String[] input_AsciiString) {
        int x = input_AsciiString.length;
        String result = "";
        for(int i = 0; i < x; i++){
            String str = Character.toString(Integer.parseInt(input_AsciiString[i]));
            result = result + str;
        }
        return result;
    }

    //Convert characters stored it in a String Array to Ascii value and stored it in a String Array 
    public static String[] convertStringToAscii(String input_String){
        char [] content = input_String.toCharArray();
        int x = content.length;
        String[] result = new String[x];
        String character;

        for(int i = 0; i < content.length; i++){
            character = "" + (int)content[i];    //converting to ascii
            result[i] = character;
        }
        return result;
    }

    //Convert Decimal to Hexadecimal
    public static String[] convertDecimalToHex(String[] input_DecimalString) {
        int x = input_DecimalString.length;
        String[] result = new String[x];

        for (int i = 0; i < x; i++) {
            BigInteger character = new BigInteger(input_DecimalString[i], 10);
            String temp = character.toString(16);
            result[i] = temp;                        
        }

        return result;
    }

    //Convert Hexadecimal to Decimal
    public static String [] convertHexToDecimal(String[] input_HexString){  
        int x = input_HexString.length; 
        String[] result = new String[x];


        for(int i = 0; i < x; i++){
            BigInteger character = new BigInteger(input_HexString[i], 16);
            String temp = character.toString();
            result[i] = temp;  
        }

        return result;
    }

}
