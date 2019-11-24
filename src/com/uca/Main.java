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
                        newP = new LIT<String>(di.substring(1,di.length()-1));
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
        stack[0] = new Data<Integer>(0, Type.INT);
        stack[1] = new Data<Integer>(0, Type.INT);
        stack[2] = new Data<Integer>(0, Type.INT);

        while (ip < codeCounter) {
            i = code[ip];
            log("IP: " + (ip) + " ");
            ip++;
            switch (i.getPcode()) {
                case LIT:
                    sp++;
                    switch (i.getNi()) {
                        case 0:
                            stack[sp] = new Data<Integer>(((LIT<Integer>) i).getValue(), Type.INT);
                            break;
                        case 1:
                            stack[sp] = new Data<Double>(((LIT<Double>) i).getValue(), Type.DEC);
                            break;
                        case 2:
                            stack[sp] = new Data<Character>(((LIT<Character>) i).getValue(), Type.CHA);
                            break;
                        case 3:
                            stack[sp] = new Data<String>(((LIT<String>) i).getValue(), Type.STR);
                            break;
                        case 4:
                            stack[sp] = new Data<Boolean>(((LIT<Boolean>) i).getValue(), Type.BOO);
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
                    stack[sp] = new Data<Integer>(base(i.getNi(), bp), Type.INT);
                    stack[sp + 1] = new Data<Integer>(bp, Type.INT);
                    stack[sp + 2] = new Data<Integer>(ip, Type.INT);
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
        stack[sp] = new Data<Integer>(scanner.nextInt(), Type.INT);
    }

    private static void oprSum() {
        sp--;
        log("OPR: Suma " + stack[sp].getValue() + " + " + stack[sp + 1].getValue());
        if (stack[sp].getType() == Type.INT && stack[sp+1].getType() == Type.INT) {
            int res = (int) stack[sp].getValue() + (int) stack[sp + 1].getValue();
            stack[sp] = new Data<Integer>(res, Type.INT);
        } else if (stack[sp].getType() == Type.DEC && stack[sp+1].getType() == Type.DEC){
            double res = (double) stack[sp].getValue() + (double) stack[sp + 1].getValue();
            stack[sp] = new Data<Double>(res, Type.DEC);
        } else if (stack[sp].getType() == Type.INT && stack[sp+1].getType() == Type.DEC){
            double res = (int) stack[sp].getValue() + (double) stack[sp + 1].getValue();
            stack[sp] = new Data<Double>(res, Type.DEC);
        } else if (stack[sp].getType() == Type.DEC && stack[sp+1].getType() == Type.INT){
            double res = (double) stack[sp].getValue() + (int) stack[sp + 1].getValue();
            stack[sp] = new Data<Double>(res, Type.DEC);
        } else if (stack[sp].getType() == Type.STR || stack[sp+1].getType() == Type.STR){
            String res = stack[sp].getValue().toString() + stack[sp + 1].getValue().toString();
            stack[sp] = new Data<String>(res, Type.STR);
        }
    }

    private static void oprSubtract() {
        sp--;
        log("OPR: Resta " + stack[sp].getValue() + " - " + stack[sp + 1].getValue());
        int res = (int) stack[sp].getValue() - (int) stack[sp + 1].getValue();
        stack[sp] = new Data<Integer>(res, Type.INT);
    }

    private static void oprMultiply() {
        sp--;
        log("OPR: Multiplicar " + stack[sp].getValue() + " * " + stack[sp + 1].getValue());
        int res = (int) stack[sp].getValue() * (int) stack[sp + 1].getValue();
        stack[sp] = new Data<Integer>(res, Type.INT);
    }

    private static void oprDivide() {
        sp--;
        log("OPR: Dividir " + stack[sp].getValue() + " / " + stack[sp + 1].getValue());
        int res = (int) stack[sp].getValue() / (int) stack[sp + 1].getValue();
        stack[sp] = new Data<Integer>(res, Type.INT);
    }

    private static void oprEqual() {
        sp--;
        log("OPR: Igual? " + stack[sp].getValue() + " == " + stack[sp + 1].getValue());
        boolean res = (int) stack[sp].getValue() == (int) stack[sp + 1].getValue();
        stack[sp] = new Data<Boolean>(res, Type.BOO);
    }

    private static void oprNotEqual() {
        sp--;
        log("OPR: No Igual? " + stack[sp].getValue() + " != " + stack[sp + 1].getValue());
        boolean res = (int) stack[sp].getValue() != (int) stack[sp + 1].getValue();
        stack[sp] = new Data<Boolean>(res, Type.BOO);
    }

    private static void oprLessThan() {
        sp--;
        log("OPR: Menor? " + stack[sp].getValue() + " < " + stack[sp + 1].getValue());
        boolean res = (int) stack[sp].getValue() < (int) stack[sp + 1].getValue();
        stack[sp] = new Data<Boolean>(res, Type.BOO);
    }

    private static void oprLessThanEqual() {
        sp--;
        log("OPR: Menor igual? " + stack[sp].getValue() + " <= " + stack[sp + 1].getValue());
        boolean res = (int) stack[sp].getValue() <= (int) stack[sp + 1].getValue();
        stack[sp] = new Data<Boolean>(res, Type.BOO);
    }

    private static void oprGreaterThan() {
        sp--;
        log("OPR: Mayor? " + stack[sp].getValue() + " > " + stack[sp + 1].getValue());
        boolean res = (int) stack[sp].getValue() > (int) stack[sp + 1].getValue();
        stack[sp] = new Data<Boolean>(res, Type.BOO);
    }

    private static void oprGreaterThanEqual() {
        sp--;
        log("OPR: Mayor igual? " + stack[sp].getValue() + " >= " + stack[sp + 1].getValue());
        boolean res = (int) stack[sp].getValue() >= (int) stack[sp + 1].getValue();
        stack[sp] = new Data<Boolean>(res, Type.BOO);
    }

    private static void oprAnd() {

    }

    private static void oprOr() {

    }

    private static void oprNot() {

    }

    private static void oprPositive() {

    }

    private static void oprNegative() {
        int res = -(int) stack[sp].getValue();
        log("OPR: Negativo - " + (int) stack[sp].getValue());
        stack[sp] = new Data<Integer>(res, Type.INT);
    }

    private static void oprMax() {

    }

    private static void oprMin() {

    }

    private static void oprRandom(){

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

    private static void oprCast(){
        int type = i.getNi();
        switch (type){
            case 0:
                if (stack[sp].getType() == Type.DEC){
                    stack[sp] = new Data<Integer>((int)Math.floor((double)stack[sp].getValue()), Type.INT);
                }else if (stack[sp].getType() == Type.CHA){
                    stack[sp] = new Data<Integer>(Character.getNumericValue((char)stack[sp].getValue()), Type.INT);
                }
                break;
            case 1:
                if (stack[sp].getType() == Type.INT){
                    stack[sp] = new Data<Double>((int)stack[sp].getValue()*1.0, Type.DEC);
                }
                break;
            case 2:
                break;
            case 3:
                if (stack[sp].getType() == Type.CHA){
                    stack[sp] = new Data<String>((char)stack[sp].getValue()+"", Type.STR);
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

    private static void log(String msg){
        if (DEBUG){
            System.out.println(msg);
        }
    }
}
