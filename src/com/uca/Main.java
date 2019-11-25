package com.uca;

import java.util.Scanner;

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
        String fileName = args[0];
        read(fileName);
        run();
    }

    private static void read(String filename) {
        ErrorLog.init();
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
        ErrorLog.close();

    }

    private static void run() {
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

    private static void oprOut() {
        log("OPR: Output");
        System.out.println(stack[sp].getValue().toString());
    }

    private static void oprIn() {
        log("OPR: Input");
        Scanner scanner = new Scanner(System.in);
        sp++;
        switch (i.getNi()){
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

    private static void oprSum() {
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

    private static void oprSubtract() {
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

    private static void oprMultiply() {
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

    private static void oprDivide() {
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

    private static void oprEqual() {
        sp--;
        log("OPR: Igual? " + stack[sp].getValue() + " == " + stack[sp + 1].getValue());
        boolean res = false;
        if (stack[sp].getType() == Type.INT && stack[sp+1].getType() == Type.INT){
            res = (int)stack[sp].getValue() == (int)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.DEC && stack[sp+1].getType() == Type.DEC){
            res = (double)stack[sp].getValue() == (double)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.CHA && stack[sp+1].getType() == Type.CHA){
            res = (char)stack[sp].getValue() == (char)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.STR && stack[sp+1].getType() == Type.STR){
            res = ((String)stack[sp].getValue()).equals((String)stack[sp + 1].getValue());
        } else if (stack[sp].getType() == Type.BOO && stack[sp+1].getType() == Type.BOO){
            res = (boolean)stack[sp].getValue() == (boolean)stack[sp + 1].getValue();
        }
        stack[sp] = createBoolean(res);
    }

    private static void oprNotEqual() {
        sp--;
        log("OPR: No Igual? " + stack[sp].getValue() + " != " + stack[sp + 1].getValue());
        boolean res = false;
        if (stack[sp].getType() == Type.INT && stack[sp+1].getType() == Type.INT){
            res = (int)stack[sp].getValue() != (int)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.DEC && stack[sp+1].getType() == Type.DEC){
            res = (double)stack[sp].getValue() != (double)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.CHA && stack[sp+1].getType() == Type.CHA){
            res = (char)stack[sp].getValue() != (char)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.STR && stack[sp+1].getType() == Type.STR){
            res = !((String)stack[sp].getValue()).equals((String)stack[sp + 1].getValue());
        } else if (stack[sp].getType() == Type.BOO && stack[sp+1].getType() == Type.BOO){
            res = (boolean)stack[sp].getValue() != (boolean)stack[sp + 1].getValue();
        }
        stack[sp] = createBoolean(res);
    }

    private static void oprLessThan() {
        sp--;
        log("OPR: Menor? " + stack[sp].getValue() + " < " + stack[sp + 1].getValue());
        boolean res = false;
        if (stack[sp].getType() == Type.INT && stack[sp+1].getType() == Type.INT){
            res = (int)stack[sp].getValue() < (int)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.DEC && stack[sp+1].getType() == Type.DEC){
            res = (double)stack[sp].getValue() < (double)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.CHA && stack[sp+1].getType() == Type.CHA){
            res = (char)stack[sp].getValue() < (char)stack[sp + 1].getValue();
        }
        stack[sp] = createBoolean(res);
    }

    private static void oprLessThanEqual() {
        sp--;
        log("OPR: Menor igual? " + stack[sp].getValue() + " <= " + stack[sp + 1].getValue());
        boolean res = false;
        if (stack[sp].getType() == Type.INT && stack[sp+1].getType() == Type.INT){
            res = (int)stack[sp].getValue() <= (int)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.DEC && stack[sp+1].getType() == Type.DEC){
            res = (double)stack[sp].getValue() <= (double)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.CHA && stack[sp+1].getType() == Type.CHA){
            res = (char)stack[sp].getValue() <= (char)stack[sp + 1].getValue();
        }
        stack[sp] = createBoolean(res);
    }

    private static void oprGreaterThan() {
        sp--;
        log("OPR: Mayor? " + stack[sp].getValue() + " > " + stack[sp + 1].getValue());
        boolean res = false;
        if (stack[sp].getType() == Type.INT && stack[sp+1].getType() == Type.INT){
            res = (int)stack[sp].getValue() > (int)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.DEC && stack[sp+1].getType() == Type.DEC){
            res = (double)stack[sp].getValue() > (double)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.CHA && stack[sp+1].getType() == Type.CHA){
            res = (char)stack[sp].getValue() > (char)stack[sp + 1].getValue();
        }
        stack[sp] = createBoolean(res);
    }

    private static void oprGreaterThanEqual() {
        sp--;
        log("OPR: Mayor igual? " + stack[sp].getValue() + " >= " + stack[sp + 1].getValue());
        boolean res = false;
        if (stack[sp].getType() == Type.INT && stack[sp+1].getType() == Type.INT){
            res = (int)stack[sp].getValue() >= (int)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.DEC && stack[sp+1].getType() == Type.DEC){
            res = (double)stack[sp].getValue() >= (double)stack[sp + 1].getValue();
        } else if (stack[sp].getType() == Type.CHA && stack[sp+1].getType() == Type.CHA){
            res = (char)stack[sp].getValue() >= (char)stack[sp + 1].getValue();
        }
        stack[sp] = createBoolean(res);
    }

    private static void oprAnd() {
        sp--;
        log("OPR: And " + stack[sp].getValue());
        if (stack[sp].getType() == Type.BOO && stack[sp + 1].getType() == Type.BOO) {
            boolean res = (boolean) stack[sp].getValue() && (boolean) stack[sp + 1].getValue();
            stack[sp] = createBoolean(res);
        }
    }

    private static void oprOr() {
        sp--;
        log("OPR: Or " + stack[sp].getValue());
        if (stack[sp].getType() == Type.BOO && stack[sp + 1].getType() == Type.BOO) {
            boolean res = (boolean) stack[sp].getValue() || (boolean) stack[sp + 1].getValue();
            stack[sp] = createBoolean(res);
        }
    }

    private static void oprNot() {
        log("OPR: Not " + stack[sp].getValue());
        if (stack[sp].getType() == Type.BOO) {
            boolean res = !(boolean) stack[sp].getValue();
            stack[sp] = createBoolean(res);
        }
    }

    private static void oprPositive() {
        log("OPR: Positivo + " + stack[sp].getValue());
    }

    private static void oprNegative() {
        log("OPR: Negativo - " + stack[sp].getValue());
        if (stack[sp].getType() == Type.INT) {
            int res = -(int) stack[sp].getValue();
            stack[sp] = createInt(res);
        } else if (stack[sp].getType() == Type.DEC) {
            double res = -(double) stack[sp].getValue();
            stack[sp] = createDouble(res);
        }
    }

    private static void oprMax() {

    }

    private static void oprMin() {

    }

    private static void oprRandom() {

    }

    private static void oprFactorial() {

    }

    private static void oprPow() {

    }

    private static void oprSqrt() {

    }

    private static void oprCeil() {

    }

    private static void oprFloor() {

    }

    private static void oprRound() {

    }

    private static void oprSubstring() {

    }

    private static void oprFileWrite() {

    }

    private static void oprFileRead() {

    }

    private static void oprCast() {
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
