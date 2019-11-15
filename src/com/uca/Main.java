package com.uca;

public class Main {

    private static final int MAX_INSTRUCTIONS = 200;
    private static final int STACK_SIZE = 500;

    private static PInstruction code[] = new PInstruction[MAX_INSTRUCTIONS];
    private static int stack[] = new int[STACK_SIZE];

    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println("Error: No se ha proporcionado el nombre del programa");
            return;
        }
        String fileName = args[0];
        read(fileName);
        run();
    }

    private static void read(String filename){
        ErrorLog.init();
        FileManager file = new FileManager(filename);
        file.openFile();
        int counter=0;
        while (!file.isEndOfFile()){
            PInstruction p = new PInstruction();
            String line = file.getNextLine();
            int i = line.indexOf(' ');
            String pcodeString = line.substring(0,i);
            PCode pc = PCode.get(pcodeString);
            p.setPcode(pc);
            line = line.substring(i+1, line.length());

            i = line.indexOf(' ');
            String niString = line.substring(0, i);
            int ni = Integer.parseInt(niString);
            p.setNi(ni);
            line = line.substring(i+1, line.length());

            String di = line;
            System.out.println("DI: "+di);
            if (pc == PCode.LIT){
                LIT newP = null;
                switch (ni){
                    case 0:
                        newP = new LIT<Integer>(Integer.parseInt(di));
                        break;
                    case 1:
                        newP = new LIT<Double>(Double.parseDouble(di));
                        break;
                    case 2:
                        newP = new LIT<Character>(di.charAt(0));
                        break;
                    case 3:
                        newP = new LIT<String>(di);
                        break;
                    case 4:
                        newP = new LIT<Boolean>(Boolean.parseBoolean(di));
                        break;
                }
                p = newP;
            }else{
                p.setDi(Integer.parseInt(di));
            }

            code[counter] = p;
            counter++;
        }
        file.clearFile();
        ErrorLog.close();

    }

    private static void run(){
        int ip=0;
        int sp=-1;
        int bp=0;
        PInstruction i;
    }

    public static void close(){
        ErrorLog.close();
        System.exit(0);
    }
}
