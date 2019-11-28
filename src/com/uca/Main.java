package com.uca;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    private static final int MAX_INSTRUCTIONS = 200;
    private static final int STACK_SIZE = 500;
    private static final boolean DEBUG = false;
    private static int codeCounter = 0;

    private static PInstruction code[] = new PInstruction[MAX_INSTRUCTIONS];
    private static Data stack[] = new Data[STACK_SIZE];
    private static Data params[] = new Data[STACK_SIZE];
    private static int paramCount = 0;

    private static int ip;
    private static int bp;
    private static int sp;
    private static PInstruction i;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: No se ha proporcionado el nombre del programa");
            return;
        }
        ErrorLog.init();
        String fileName = args[0];
        read(fileName);
        try {
            run();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.logError("Error: Runtime error");
        }
        ErrorLog.close();
    }

    private static void read(String filename) {
        FileManager file = new FileManager(filename);
        file.openFile();
        codeCounter = 0;
        while (!file.isEndOfFile()) {
            PInstruction p = new PInstruction();
            String line = file.getNextLine();
            if (line.isEmpty()) {
                continue;
            }
            int i = line.indexOf(' ');
            String pcodeString = line.substring(0, i);
            PCode pc = PCode.get(pcodeString);
            p.setPcode(pc);
            line = line.substring(i + 1, line.length());

            i = line.indexOf(' ');
            String niString = line.substring(0, i);
            int ni = Integer.parseInt(niString);
            p.setNi(ni);
            line = line.substring(i + 1, line.length());

            String di = line;
            if (pc == PCode.LIT) {
                LIT newP = null;
                switch (ni) {
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
                        newP = new LIT<String>(di.substring(1, di.length() - 1));
                        break;
                    case 4:
                        newP = new LIT<Boolean>(Boolean.parseBoolean(di));
                        break;
                }
                newP.setPcode(p.getPcode());
                newP.setNi(p.getNi());
                p = newP;
            } else {
                p.setDi(Integer.parseInt(di));
            }

            code[codeCounter] = p;
            codeCounter++;
        }
        file.closeFile();
    }

    private static void run() throws Exception {
        ip = 0;
        sp = -1;
        bp = 0;
        stack[0] = createInt(0);
        stack[1] = createInt(0);
        stack[2] = createInt(0);

        while (ip < codeCounter) {
            if (sp > STACK_SIZE) {
                ErrorLog.logError("Error: Stack overflow");
            }
            i = code[ip];
            log("IP: " + (ip) + " ");
            ip++;
            switch (i.getPcode()) {
                case LIT:
                    sp++;
                    switch (i.getNi()) {
                        case 0:
                            stack[sp] = createInt(((LIT<Integer>) i).getValue());
                            break;
                        case 1:
                            stack[sp] = createDouble(((LIT<Double>) i).getValue());
                            break;
                        case 2:
                            stack[sp] = createChar(((LIT<Character>) i).getValue());
                            break;
                        case 3:
                            stack[sp] = createString(((LIT<String>) i).getValue());
                            break;
                        case 4:
                            stack[sp] = createBoolean(((LIT<Boolean>) i).getValue());
                            break;
                    }
                    log("LIT: Cargando el valor " + stack[sp].getValue().toString() + " en la direccion " + sp);
                    break;
                case OPR:
                    switch (i.getDi()) {
                        case 0:
                            break;
                        case 1:
                            oprOut();
                            break;
                        case 2:
                            oprIn();
                            break;
                        case 3:
                            oprSum();
                            break;
                        case 4:
                            oprSubtract();
                            break;
                        case 5:
                            oprMultiply();
                            break;
                        case 6:
                            oprDivide();
                            break;
                        case 7:
                            oprEqual();
                            break;
                        case 8:
                            oprNotEqual();
                            break;
                        case 9:
                            oprLessThan();
                            break;
                        case 10:
                            oprLessThanEqual();
                            break;
                        case 11:
                            oprGreaterThan();
                            break;
                        case 12:
                            oprGreaterThanEqual();
                            break;
                        case 13:
                            oprAnd();
                            break;
                        case 14:
                            oprOr();
                            break;
                        case 15:
                            oprNot();
                            break;
                        case 16:
                            oprPositive();
                            break;
                        case 17:
                            oprNegative();
                            break;
                        case 18:
                            oprMax();
                            break;
                        case 19:
                            oprMin();
                            break;
                        case 20:
                            oprRandom();
                            break;
                        case 21:
                            oprFactorial();
                            break;
                        case 22:
                            oprPow();
                            break;
                        case 23:
                            oprSqrt();
                            break;
                        case 24:
                            oprCeil();
                            break;
                        case 25:
                            oprFloor();
                            break;
                        case 26:
                            oprRound();
                            break;
                        case 27:
                            oprSubstring();
                            break;
                        case 28:
                            oprFileWrite();
                            break;
                        case 29:
                            oprFileRead();
                            break;
                        case 30:
                            oprFileClear();
                            break;
                        case 31:
                            oprCast();
                            break;
                    }
                    break;
                case CAR:
                    sp++;
                    stack[sp] = stack[base(i.getNi(), bp) + i.getDi()];
                    log("CAR: Cargando de la direccion " + (base(i.getNi(), bp) + i.getDi()) + " el valor " + stack[sp].getValue().toString() + " a la direccion " + sp);
                    break;
                case ALM:
                    log("ALM: Almacenando " + stack[sp].getValue().toString() + " en la direccion " + (base(i.getNi(), bp) + i.getDi()));
                    stack[base(i.getNi(), bp) + i.getDi()] = stack[sp];
                    sp--;
                    break;
                case LLA:
                    sp++;
                    stack[sp] = createInt(base(i.getNi(), bp));
                    stack[sp + 1] = createInt(bp);
                    stack[sp + 2] = createInt(ip);
                    bp = sp;
                    ip = i.getDi();
                    log("LLA: Llamada a funcion en linea " + ip);
                    int m = 0;
                    for (int k = paramCount - 1; k >= 0; k--) {
                        stack[sp + 3 + m] = params[k];
                        m++;
                        log("LLA: Cargando parametro " + params[k].getValue() + " a la direccion " + (sp + 3 + k));
                    }
                    break;
                case INS:
                    log("INS: Asignando " + i.getDi() + " espacios en el stack");
                    sp += i.getDi();
                    break;
                case SAL:
                    ip = i.getDi();
                    log("SAL: Salto incondicional a la linea " + i.getDi());
                    break;
                case SAC:
                    if (!(boolean) stack[sp].getValue()) {
                        ip = i.getDi();
                        log("SAC: Salto condicional a la linea " + i.getDi());
                    } else {
                        log("SAC: No salto");
                    }
                    sp--;
                    break;
                case SAI:
                    if ((boolean) stack[sp].getValue()) {
                        ip = i.getDi();
                        log("SAI: Salto condicional a la linea " + i.getDi());
                    } else {
                        log("SAI: No salto");
                    }
                    sp--;
                    break;
                case ALO:
                    log("ALO: Almacenando " + stack[sp].getValue().toString() + " en el arreglo " + (base(i.getNi(), bp) + i.getDi()) + " posicion " + (int) stack[sp - 1].getValue());
                    stack[base(i.getNi(), bp) + i.getDi() + (int) stack[sp - 1].getValue()] = stack[sp];
                    sp -= 2;
                    break;
                case CAO:
                    int offset = (int) stack[sp].getValue();
                    if (stack[base(i.getNi(), bp) + i.getDi() + offset] == null){
                        ErrorLog.logError("Error: No se ha inicializado el arreglo en la posicion "+offset);
                        Main.close();
                    }
                    stack[sp] = stack[base(i.getNi(), bp) + i.getDi() + offset];
                    log("CAO: Cargando del arreglo " + (base(i.getNi(), bp) + i.getDi()) + " posicion " + offset + " el valor " + stack[sp].getValue().toString() + " a la direccion " + sp);
                    break;
                case PAR:
                    paramCount = i.getDi();
                    for (int k = 0; k < paramCount; k++) {
                        params[k] = stack[sp];
                        sp--;
                    }
                    log("PAR: Almacenando " + paramCount + " parametros");
                    break;
                case RET:
                    Data returnVal = stack[sp];
                    sp = bp;
                    ip = (int) stack[sp + 2].getValue();
                    bp = (int) stack[sp + 1].getValue();
                    stack[sp] = returnVal;
                    log("RET: Retornar a la instruccion " + ip);
                    break;
            }
            log("SP " + sp);
            printStack();
            log("");
        }
    }

    private static void oprOut() throws Exception {
        log("OPR: Output");
        System.out.println(stack[sp].getValue().toString());
    }

    private static void oprIn() throws Exception {
        log("OPR: Input");
        Scanner scanner = new Scanner(System.in);
        sp++;
        switch (i.getNi()) {
            case 0:
                stack[sp] = createInt(Integer.parseInt(scanner.nextLine()));
                break;
            case 1:
                stack[sp] = createDouble(Double.parseDouble(scanner.nextLine()));
                break;
            case 2:
                stack[sp] = createChar(scanner.nextLine().charAt(0));
                break;
            case 3:
                stack[sp] = createString(scanner.nextLine());
                break;
            case 4:
                stack[sp] = createBoolean(Boolean.parseBoolean(scanner.nextLine()));
                break;
        }
    }

    private static void oprSum() throws Exception {
        sp--;
        log("OPR: Suma " + stack[sp].getValue() + " + " + stack[sp + 1].getValue());
        if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.INT) {
            int res = (int) stack[sp].getValue() + (int) stack[sp + 1].getValue();
            stack[sp] = createInt(res);
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.DEC) {
            double res = (double) stack[sp].getValue() + (double) stack[sp + 1].getValue();
            stack[sp] = createDouble(res);
        } else if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.DEC) {
            double res = (int) stack[sp].getValue() + (double) stack[sp + 1].getValue();
            stack[sp] = createDouble(res);
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.INT) {
            double res = (double) stack[sp].getValue() + (int) stack[sp + 1].getValue();
            stack[sp] = createDouble(res);
        } else if (stack[sp].getType() == Type.STR || stack[sp + 1].getType() == Type.STR) {
            String res = stack[sp].getValue().toString() + stack[sp + 1].getValue().toString();
            stack[sp] = createString(res);
        }
    }

    private static void oprSubtract() throws Exception {
        sp--;
        log("OPR: Resta " + stack[sp].getValue() + " - " + stack[sp + 1].getValue());
        if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.INT) {
            int res = (int) stack[sp].getValue() - (int) stack[sp + 1].getValue();
            stack[sp] = createInt(res);
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.DEC) {
            double res = (double) stack[sp].getValue() - (double) stack[sp + 1].getValue();
            stack[sp] = createDouble(res);
        } else if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.DEC) {
            double res = (int) stack[sp].getValue() - (double) stack[sp + 1].getValue();
            stack[sp] = createDouble(res);
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.INT) {
            double res = (double) stack[sp].getValue() - (int) stack[sp + 1].getValue();
            stack[sp] = createDouble(res);
        }
    }

    private static void oprMultiply() throws Exception {
        sp--;
        log("OPR: Multiplicar " + stack[sp].getValue() + " * " + stack[sp + 1].getValue());
        if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.INT) {
            int res = (int) stack[sp].getValue() * (int) stack[sp + 1].getValue();
            stack[sp] = createInt(res);
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.DEC) {
            double res = (double) stack[sp].getValue() * (double) stack[sp + 1].getValue();
            stack[sp] = createDouble(res);
        } else if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.DEC) {
            double res = (int) stack[sp].getValue() * (double) stack[sp + 1].getValue();
            stack[sp] = createDouble(res);
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.INT) {
            double res = (double) stack[sp].getValue() * (int) stack[sp + 1].getValue();
            stack[sp] = createDouble(res);
        }
    }

    private static void oprDivide() throws Exception {
        sp--;
        log("OPR: Dividir " + stack[sp].getValue() + " / " + stack[sp + 1].getValue());
        if (stack[sp + 1].getType() == Type.INT && (int) stack[sp + 1].getValue() == 0) {
            ErrorLog.logError("Error: Division entre 0");
        }
        if (stack[sp + 1].getType() == Type.DEC && (double) stack[sp + 1].getValue() == 0.0) {
            ErrorLog.logError("Error: Division entre 0");
        }
        if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.INT) {
            int res = (int) stack[sp].getValue() / (int) stack[sp + 1].getValue();
            stack[sp] = createInt(res);
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.DEC) {
            double res = (double) stack[sp].getValue() / (double) stack[sp + 1].getValue();
            stack[sp] = createDouble(res);
        } else if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.DEC) {
            double res = (int) stack[sp].getValue() / (double) stack[sp + 1].getValue();
            stack[sp] = createDouble(res);
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.INT) {
            double res = (double) stack[sp].getValue() / (int) stack[sp + 1].getValue();
            stack[sp] = createDouble(res);
        }
    }

    private static void oprEqual() throws Exception {
        sp--;
        log("OPR: Igual? " + stack[sp].getValue() + " == " + stack[sp + 1].getValue());
        boolean res = false;
        if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.INT) {
            res = (int) stack[sp].getValue() == (int) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.DEC) {
            res = (double) stack[sp].getValue() == (double) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.CHA && stack[sp + 1].getType() == Type.CHA) {
            res = (char) stack[sp].getValue() == (char) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.STR && stack[sp + 1].getType() == Type.STR) {
            res = ((String) stack[sp].getValue()).equals((String) stack[sp + 1].getValue());
        } else if (stack[sp].getType() == Type.BOO && stack[sp + 1].getType() == Type.BOO) {
            res = (boolean) stack[sp].getValue() == (boolean) stack[sp + 1].getValue();
        }
        stack[sp] = createBoolean(res);
    }

    private static void oprNotEqual() throws Exception {
        sp--;
        log("OPR: No Igual? " + stack[sp].getValue() + " != " + stack[sp + 1].getValue());
        boolean res = false;
        if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.INT) {
            res = (int) stack[sp].getValue() != (int) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.DEC) {
            res = (double) stack[sp].getValue() != (double) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.CHA && stack[sp + 1].getType() == Type.CHA) {
            res = (char) stack[sp].getValue() != (char) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.STR && stack[sp + 1].getType() == Type.STR) {
            res = !((String) stack[sp].getValue()).equals((String) stack[sp + 1].getValue());
        } else if (stack[sp].getType() == Type.BOO && stack[sp + 1].getType() == Type.BOO) {
            res = (boolean) stack[sp].getValue() != (boolean) stack[sp + 1].getValue();
        }
        stack[sp] = createBoolean(res);
    }

    private static void oprLessThan() throws Exception {
        sp--;
        log("OPR: Menor? " + stack[sp].getValue() + " < " + stack[sp + 1].getValue());
        boolean res = false;
        if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.INT) {
            res = (int) stack[sp].getValue() < (int) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.DEC) {
            res = (double) stack[sp].getValue() < (double) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.CHA && stack[sp + 1].getType() == Type.CHA) {
            res = (char) stack[sp].getValue() < (char) stack[sp + 1].getValue();
        }
        stack[sp] = createBoolean(res);
    }

    private static void oprLessThanEqual() throws Exception {
        sp--;
        log("OPR: Menor igual? " + stack[sp].getValue() + " <= " + stack[sp + 1].getValue());
        boolean res = false;
        if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.INT) {
            res = (int) stack[sp].getValue() <= (int) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.DEC) {
            res = (double) stack[sp].getValue() <= (double) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.CHA && stack[sp + 1].getType() == Type.CHA) {
            res = (char) stack[sp].getValue() <= (char) stack[sp + 1].getValue();
        }
        stack[sp] = createBoolean(res);
    }

    private static void oprGreaterThan() throws Exception {
        sp--;
        log("OPR: Mayor? " + stack[sp].getValue() + " > " + stack[sp + 1].getValue());
        boolean res = false;
        if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.INT) {
            res = (int) stack[sp].getValue() > (int) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.DEC) {
            res = (double) stack[sp].getValue() > (double) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.CHA && stack[sp + 1].getType() == Type.CHA) {
            res = (char) stack[sp].getValue() > (char) stack[sp + 1].getValue();
        }
        stack[sp] = createBoolean(res);
    }

    private static void oprGreaterThanEqual() throws Exception {
        sp--;
        log("OPR: Mayor igual? " + stack[sp].getValue() + " >= " + stack[sp + 1].getValue());
        boolean res = false;
        if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.INT) {
            res = (int) stack[sp].getValue() >= (int) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.DEC) {
            res = (double) stack[sp].getValue() >= (double) stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.CHA && stack[sp + 1].getType() == Type.CHA) {
            res = (char) stack[sp].getValue() >= (char) stack[sp + 1].getValue();
        }
        stack[sp] = createBoolean(res);
    }

    private static void oprAnd() throws Exception {
        sp--;
        log("OPR: And " + stack[sp].getValue());
        if (stack[sp].getType() == Type.BOO && stack[sp + 1].getType() == Type.BOO) {
            boolean res = (boolean) stack[sp].getValue() && (boolean) stack[sp + 1].getValue();
            stack[sp] = createBoolean(res);
        }
    }

    private static void oprOr() throws Exception {
        sp--;
        log("OPR: Or " + stack[sp].getValue());
        if (stack[sp].getType() == Type.BOO && stack[sp + 1].getType() == Type.BOO) {
            boolean res = (boolean) stack[sp].getValue() || (boolean) stack[sp + 1].getValue();
            stack[sp] = createBoolean(res);
        }
    }

    private static void oprNot() throws Exception {
        log("OPR: Not " + stack[sp].getValue());
        if (stack[sp].getType() == Type.BOO) {
            boolean res = !(boolean) stack[sp].getValue();
            stack[sp] = createBoolean(res);
        }
    }

    private static void oprPositive() throws Exception {
        log("OPR: Positivo + " + stack[sp].getValue());
    }

    private static void oprNegative() throws Exception {
        log("OPR: Negativo - " + stack[sp].getValue());
        if (stack[sp].getType() == Type.INT) {
            int res = -(int) stack[sp].getValue();
            stack[sp] = createInt(res);
        } else if (stack[sp].getType() == Type.DEC) {
            double res = -(double) stack[sp].getValue();
            stack[sp] = createDouble(res);
        }
    }

    private static void oprMax() throws Exception {
        sp--;
        log("OPR: Max between two " + stack[sp].getValue() + " ? " + stack[sp + 1].getValue());
        if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.INT) {
            if((int) stack[sp].getValue() > (int) stack[sp + 1].getValue()){
                stack[sp]=createInt((int) stack[sp].getValue());
            }else {
                stack[sp]=createInt((int) stack[sp + 1].getValue());
            }
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.DEC) {
            if((double) stack[sp].getValue() > (double) stack[sp + 1].getValue()){
                stack[sp]=createDouble((double) stack[sp].getValue());
            }else {
                stack[sp]=createDouble((double) stack[sp + 1].getValue());
            }
        } else if (stack[sp].getType() == Type.CHA && stack[sp + 1].getType() == Type.CHA) {
            if((char) stack[sp].getValue() > (char) stack[sp + 1].getValue()){
                stack[sp]=createChar((char) stack[sp].getValue());
            }else {
                stack[sp]=createChar((char) stack[sp + 1].getValue());
            }
        }
    }

    private static void oprMin() throws Exception {
        sp--;
        log("OPR: Min between two " + stack[sp].getValue() + " ? " + stack[sp + 1].getValue());
        if (stack[sp].getType() == Type.INT && stack[sp + 1].getType() == Type.INT) {
            if((int) stack[sp].getValue() < (int) stack[sp + 1].getValue()){
                stack[sp]=createInt((int) stack[sp].getValue());
            }else {
                stack[sp]=createInt((int) stack[sp + 1].getValue());
            }
        } else if (stack[sp].getType() == Type.DEC && stack[sp + 1].getType() == Type.DEC) {
            if((double) stack[sp].getValue() < (double) stack[sp + 1].getValue()){
                stack[sp]=createDouble((double) stack[sp].getValue());
            }else {
                stack[sp]=createDouble((double) stack[sp + 1].getValue());
            }
        } else if (stack[sp].getType() == Type.CHA && stack[sp + 1].getType() == Type.CHA) {
            if((char) stack[sp].getValue() < (char) stack[sp + 1].getValue()){
                stack[sp]=createChar((char) stack[sp].getValue());
            }else {
                stack[sp]=createChar((char) stack[sp + 1].getValue());
            }
        }
    }

    private static void oprRandom() throws Exception {
        log("OPR Random between" +(int)stack[sp].getValue() +"And"+ (int)stack[sp+1].getValue());
        sp--;
        int randomNum = ThreadLocalRandom.current().nextInt((int)stack[sp].getValue(), (int)stack[sp+1].getValue() + 1);
        stack[sp]=createInt(randomNum);
    }

    private static void oprFactorial() throws Exception {
        log("OPR Factorial " + stack[sp].getValue());
        int factorial =1;
        int number = (int) stack[sp].getValue();

        while(number != 0) {
            factorial = factorial * number;
            number--;
        }
        stack[sp]=createInt(factorial);
    }

    private static void oprPow() throws Exception {
        sp--;
        log("Pow " + stack[sp] +"^"+stack[sp+1]);
        if(stack[sp].getType()==Type.INT){
            int res=1;
            for (int i=0;i<(int)stack[sp+1].getValue();i++){
                res=res*(int)stack[sp].getValue();
            }
            stack[sp]=createInt(res);
        }
        if(stack[sp].getType()==Type.DEC){
            Double res =1.0;
            for (int i=0;i<(int)stack[sp+1].getValue();i++){
                res=res*(int)stack[sp].getValue();
            }
            stack[sp]=createDouble(res);
        }
    }

    private static void oprSqrt() throws Exception {
        {
            log("OPR Sqrt "+ stack[sp].getValue());
            double i=0;
            double x1,x2=1;
            double m= (double)stack[sp].getValue();
            while( (i*i) <= m ) {
                i += 0.1;
            }
            x1=i;
            for(int j=0;j<15;j++)
            {
                x2=m;
                x2/=x1;
                x2+=x1;
                x2/=2;
                x1=x2;
            }

            stack[sp]=createDouble(x2);
        }
    }

    private static void oprCeil() throws Exception {
        log("Ceiling "+ stack[sp].getValue());
        if (stack[sp].getType()==Type.DEC){
            if(((double)stack[sp].getValue()*10)%10 != 0){
                stack[sp]=createDouble(((double)stack[sp].getValue()-((double)stack[sp].getValue()*10%10)/10)+1);
            }
        }
    }

    private static void oprFloor() throws Exception {
        log("Floor "+ stack[sp].getValue());
        if (stack[sp].getType()==Type.DEC){
            if(((double)stack[sp].getValue()*10)%10 != 0){
                stack[sp]=createDouble((double)stack[sp].getValue()-((double)stack[sp].getValue()*10%10)/10);
            }
        }
    }

    private static void oprRound() throws Exception {
        log("Round "+ stack[sp].getValue());
        if (stack[sp].getType()==Type.DEC){
            if(((double)stack[sp].getValue()*10)%10 >= 5){
                stack[sp]=createDouble(((double)stack[sp].getValue()-((double)stack[sp].getValue()*10%10)/10)+1);
            }else {
                stack[sp]=createDouble((double)stack[sp].getValue()-((double)stack[sp].getValue()*10%10)/10);
            }
        }
    }

    private static void oprSubstring() throws Exception {
        sp--;
        sp--;
        String subs = (String) stack[sp].getValue();
        String newString;
        newString=subs.substring((int)stack[sp+1].getValue(),(int)stack[sp+2].getValue());
        stack[sp]=createString(newString);
    }

    private static void oprFileWrite() throws Exception {
        log("OPR: File Write");
        sp -= 2;
        FileManager file = new FileManager(stack[sp+1].getValue().toString());
        file.createFile();
        file.writeLine(stack[sp].getValue().toString());
        file.closeFile();
    }

    private static void oprFileRead() throws Exception {
        log("OPR: File Read");
        FileManager file = new FileManager(stack[sp].getValue().toString());
        file.openFile();
        switch (i.getNi()) {
            case 0:
                stack[sp] = createInt(Integer.parseInt(file.getNextLine()));
                break;
            case 1:
                stack[sp] = createDouble(Double.parseDouble(file.getNextLine()));
                break;
            case 2:
                stack[sp] = createChar(file.getNextLine().charAt(0));
                break;
            case 3:
                stack[sp] = createString(file.getNextLine());
                break;
            case 4:
                stack[sp] = createBoolean(Boolean.parseBoolean(file.getNextLine()));
                break;
        }
        file.closeFile();
    }

    private static void oprFileClear() throws Exception {
        log("OPR: File Clear");
        FileManager file = new FileManager(stack[sp].getValue().toString());
        file.createFile();
        file.clearFile();
        file.closeFile();
        sp--;
    }

    private static void oprCast() throws Exception {
        log("OPR: Cast");
        int type = i.getNi();
        switch (type) {
            case 0:
                if (stack[sp].getType() == Type.DEC) {
                    stack[sp] = createInt((int) Math.floor((double) stack[sp].getValue()));
                } else if (stack[sp].getType() == Type.CHA) {
                    stack[sp] = createInt(Character.getNumericValue((char) stack[sp].getValue()));
                }
                break;
            case 1:
                if (stack[sp].getType() == Type.INT) {
                    stack[sp] = createDouble((int) stack[sp].getValue() * 1.0);
                }
                break;
            case 2:
                break;
            case 3:
                if (stack[sp].getType() == Type.CHA) {
                    stack[sp] = createString((char) stack[sp].getValue() + "");
                }
                break;
            case 4:
                break;
        }
    }

    private static int base(int ni, int b) {
        int b1;
        b1 = b;
        while (ni > 0) {
            b1 = (int) stack[b1].getValue();
            ni--;
        }
        return b1;
    }

    private static Data createInt(int val) {
        return new Data<Integer>(val, Type.INT);
    }

    private static Data createDouble(double val) {
        return new Data<Double>(val, Type.DEC);
    }

    private static Data createChar(char val) {
        return new Data<Character>(val, Type.CHA);
    }

    private static Data createString(String val) {
        return new Data<String>(val, Type.STR);
    }

    private static Data createBoolean(boolean val) {
        return new Data<Boolean>(val, Type.BOO);
    }

    public static void close() {
        ErrorLog.close();
        System.exit(0);
    }

    private static void printStack() {
        if (DEBUG) {
            for (int i = 0; i < stack.length; i++) {
                if (stack[i] != null) {
                    System.out.print(stack[i].getValue().toString() + " ");
                } else {
                    System.out.print("_ ");
                }
            }
            System.out.println();
        }
    }

    private static void log(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }
}
