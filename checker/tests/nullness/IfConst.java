public class IfConst {
    class LiteralTrue {
        String foo() {
            final String ans;
            if (true) ans = "foo";
            return ans;
        }
        String foo2() {
            String ans;
            if (true) ans = "foo";
            return ans;
        }
        String foo3() {
            String ans = null;
            if (true) ans = "foo";
            return ans;
        }

        class EmptyElse {
            String foo() {
                final String ans;
                if (true) ans = "foo"; else {}
                return ans;
            }
            String foo2() {
                String ans;
                if (true) ans = "foo"; else {}
                return ans;
            }
            String foo3() {
                String ans = null;
                if (true) ans = "foo"; else {}
                return ans;
            }
        }

        class NonEmptyElse {
            String foo() {
                final String ans;
                if (true) ans = "foo"; else ans = null;
                return ans;
            }
            String foo2() {
                String ans;
                if (true) ans = "foo"; else ans = null;
                return ans;
            }
            String foo3() {
                String ans = null;
                if (true) ans = "foo"; else ans = null;
                return ans;
            }
        }
    }

    class LiteralFalse {
        class EmptyThen {
            String foo() {
                final String ans;
                if (false) {} else ans = "foo";
                return ans;
            }
            String foo2() {
                String ans;
                if (false) {} else ans = "foo";
                return ans;
            }
            String foo3() {
                String ans = null;
                if (false) {} else ans = "foo";
                return ans;
            }
        }

        class NonEmptyThen {
            String foo() {
                final String ans;
                if (false) ans = null; else ans = "foo";
                return ans;
            }
            String foo2() {
                String ans;
                if (false) ans = null; else ans = "foo";
                return ans;
            }
            String foo3() {
                String ans = null;
                if (false) ans = null; else ans = "foo";
                return ans;
            }
        }
    }

    class Nested {
        class LiteralTrueFalse {
            String foo() {
                final String ans;
                if (true) {
                    ans = "foo";
                } else if (false) {
                    ans = null;
                } else {
                    ans = null;
                }
                return ans;
            }
            String foo2() {
                String ans;
                if (true) {
                    ans = "foo";
                } else if (false) {
                    ans = null;
                } else {
                    ans = null;
                }
                return ans;
            }
            String foo3() {
                String ans = null;
                if (true) {
                    ans = "foo";
                } else if (false) {
                    ans = null;
                } else {
                    ans = null;
                }
                return ans;
            }
        }

        class LiteralFalseTrue {
            String foo() {
                final String ans;
                if (false) {
                    ans = null;
                } else if (true) {
                    ans = "foo";
                } else {
                    ans = null;
                }
                return ans;
            }
            String foo2() {
                String ans;
                if (false) {
                    ans = null;
                } else if (true) {
                    ans = "foo";
                } else {
                    ans = null;
                }
                return ans;
            }
            String foo3() {
                String ans = null;
                if (false) {
                    ans = null;
                } else if (true) {
                    ans = "foo";
                } else {
                    ans = null;
                }
                return ans;
            }
        }
    }

    class DeadCodeMustStillTypeCheck {
        class LiteralTrue {
            String foo() {
                if (true) {
                    return "foo";
                } else {
                    //:: error: (return.type.incompatible)
                    return null;
                }
            }
            String foo2() {
                if (true) {
                    return "foo";
                }
                //:: error: (return.type.incompatible)
                return null;
            }
            String nested() {
                if (true) {
                    return "foo";
                } else if (false) {
                    //:: error: (return.type.incompatible)
                    return null;
                } else {
                    //:: error: (return.type.incompatible)
                    return null;
                }
            }
        }

        class LiteralFalse {
            String foo() {
                if (false) {
                    //:: error: (return.type.incompatible)
                    return null;
                } else {
                    return "foo";
                }
            }
            String foo2() {
                if (false) {
                    //:: error: (return.type.incompatible)
                    return null;
                }
                return "foo";
            }
        }
    }

    class DeadCodeDoesNotCompleteNormally {
        Object x;
        DeadCodeDoesNotCompleteNormally() {
            if (true) {} else {}
            x = new Object();
        }
    }

    class ReturnOnlyReachableViaDeadCode {
        public String foo() {
            final String ans;
            if (false) {
                ans = "foo";
            }
            else {
                throw new Error();
            }
            return ans;
        }
    }

    class NonConstantVariable {
        // (TRUE) is not a "constant expression" per JLS 15.28,
        // because TRUE is not a "constant variable" per JLS 4.12.4
        // (because it is not final).
        String foo() {
            boolean TRUE = true;
            String ans = null;
            if (TRUE) ans = "foo";
            //:: error: (return.type.incompatible)
            return ans;
        }
    }

    class OtherConstantExpressions {
        class VariousBooleanExpressions {
            String notFalse() {
                final String ans;
                if (!false) ans = "foo";
                return ans;
            }
            String notNotTrue() {
                final String ans;
                if (!!true) ans = "foo";
                return ans;
            }
            String TrueOrFalse() {
                final String ans;
                if (true||false) ans = "foo";
                return ans;
            }
            String FalseOrTrue() {
                final String ans;
                if (false||true) ans = "foo";
                return ans;
            }
        }

        class Variable {
            String foo() {
                final boolean TRUE = true;
                final String ans;
                if (TRUE) ans = "foo";
                return ans;
            }
        }

        class Field {
            final boolean TRUE = true;
            String foo() {
                final String ans;
                if (TRUE) ans = "foo";
                return ans;
            }
        }

        class StaticField {
            static final boolean TRUE = true;
            String foo() {
                final String ans;
                if (TRUE) ans = "foo";
                return ans;
            }
        }
    }
}
