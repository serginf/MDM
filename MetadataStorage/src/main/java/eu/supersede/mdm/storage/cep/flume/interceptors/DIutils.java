package eu.supersede.mdm.storage.cep.flume.interceptors;


public class DIutils {

    public static boolean compare(String op1, String type, String op2, String o) {


        switch (o) {
            case "eq": {
                if (!op1.equals(op2)) {
                    return true;
                }
                break;
            }
            case "nq": {
                if (op1.equals(op2)) {
                    return true;
                }
                break;
            }
        }


        switch (type) {
            case DistributedInterceptorConstants.TYPE_INT: {
                switch (o) {
                    case "gt": {
                        if (Integer.parseInt(op1) >= Integer.parseInt(op2)) {
                            return true;
                        }
                        break;
                    }
                    case "ge": {
                        if (Integer.parseInt(op1) > Integer.parseInt(op2)) {
                            return true;
                        }
                        break;
                    }
                    case "lt": {
                        if (Integer.parseInt(op1) <= Integer.parseInt(op2)) {
                            return true;
                        }
                        break;
                    }
                    case "le": {
                        if (Integer.parseInt(op1) < Integer.parseInt(op2)) {
                            return true;
                        }
                        break;
                    }
                }
                break;
            }
            case DistributedInterceptorConstants.TYPE_DOUBLE: {
                switch (o) {
                    case "gt": {
                        if (Double.parseDouble(op1) >= Double.parseDouble(op2)) {
                            return true;
                        }
                        break;
                    }
                    case "ge": {
                        if (Double.parseDouble(op1) > Double.parseDouble(op2)) {
                            return true;
                        }
                        break;
                    }
                    case "lt": {
                        if (Double.parseDouble(op1) <= Double.parseDouble(op2)) {
                            return true;
                        }
                        break;
                    }
                    case "le": {
                        if (Double.parseDouble(op1) < Double.parseDouble(op2)) {
                            return true;
                        }
                        break;
                    }
                }

                break;
            }
            case DistributedInterceptorConstants.TYPE_FLOAT: {
                switch (o) {
                    case "gt": {
                        if (Float.parseFloat(op1) >= Float.parseFloat(op2)) {
                            return true;
                        }
                        break;
                    }
                    case "ge": {
                        if (Float.parseFloat(op1) > Float.parseFloat(op2)) {
                            return true;
                        }
                        break;
                    }
                    case "lt": {
                        if (Float.parseFloat(op1) <= Float.parseFloat(op2)) {
                            return true;
                        }
                        break;
                    }
                    case "le": {
                        if (Float.parseFloat(op1) < Float.parseFloat(op2)) {
                            return true;
                        }
                        break;
                    }
                }

                break;
            }
            case DistributedInterceptorConstants.TYPE_LONG: {
                switch (o) {
                    case "gt": {
                        if (Long.parseLong(op1) >= Long.parseLong(op2)) {
                            return true;
                        }
                        break;
                    }
                    case "ge": {
                        if (Long.parseLong(op1) > Long.parseLong(op2)) {
                            return true;
                        }
                        break;
                    }
                    case "lt": {
                        if (Long.parseLong(op1) <= Long.parseLong(op2)) {
                            return true;
                        }
                        break;
                    }
                    case "le": {
                        if (Long.parseLong(op1) < Long.parseLong(op2)) {
                            return true;
                        }
                        break;
                    }
                }
                break;
            }
        }
        return false;
    }
}