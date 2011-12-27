/**
 * Class constants from /sun/com/java/Constants.java  
 */

package oqube.bytes;

public interface Constants {

    /* Signature Characters */
    char   SIGC_VOID                  = 'V';
    String SIG_VOID                   = "V";
    char   SIGC_BOOLEAN               = 'Z';
    String SIG_BOOLEAN                = "Z";
    char   SIGC_BYTE                  = 'B';
    String SIG_BYTE                   = "B";
    char   SIGC_CHAR                  = 'C';
    String SIG_CHAR                   = "C";
    char   SIGC_SHORT                 = 'S';
    String SIG_SHORT                  = "S";
    char   SIGC_INT                   = 'I';
    String SIG_INT                    = "I";
    char   SIGC_LONG                  = 'J';
    String SIG_LONG                   = "J";
    char   SIGC_FLOAT                 = 'F';
    String SIG_FLOAT                  = "F";
    char   SIGC_DOUBLE                = 'D';
    String SIG_DOUBLE                 = "D";
    char   SIGC_ARRAY                 = '[';
    String SIG_ARRAY                  = "[";
    char   SIGC_CLASS                 = 'L';
    String SIG_CLASS                  = "L";
    char   SIGC_METHOD                = '(';
    String SIG_METHOD                 = "(";
    char   SIGC_ENDCLASS              = ';';
    String SIG_ENDCLASS               = ";";
    char   SIGC_ENDMETHOD             = ')';
    String SIG_ENDMETHOD              = ")";
    char   SIGC_PACKAGE               = '/';
    String SIG_PACKAGE                = "/";
    
    /* signature chars for annotation values - Java 5 */
    char   SIGC_STRING                = 's';
    String SIG_STRING                 = "s";
    char   SIGC_ENUM                  = 'e';
    String SIG_ENUM                   = "e";
    char   SIGC_CLASS_REF             = 'c';
    String SIG_CLASS_REF              = "c";
    char   SIGC_ANNOTATION            = '@';
    String SIG_ANNOTATION             = "@";
    
    /* Class File Constants */
    int JAVA_MAGIC                   = 0xcafebabe;
    int JAVA_MIN_SUPPORTED_VERSION   = 45;
    int JAVA_MAX_SUPPORTED_VERSION   = 50;
    int JAVA_MAX_SUPPORTED_MINOR_VERSION = 0;

    /* Generate class file version for 1.4  by default */
    int JAVA_DEFAULT_VERSION         = 48;
    int JAVA_DEFAULT_MINOR_VERSION   = 0;

    /* Constant table */
    int CONSTANT_UTF8                = 1;
    int CONSTANT_UNICODE             = 2;
    int CONSTANT_INTEGER             = 3;
    int CONSTANT_FLOAT               = 4;
    int CONSTANT_LONG                = 5;
    int CONSTANT_DOUBLE              = 6;
    int CONSTANT_CLASS               = 7;
    int CONSTANT_STRING              = 8;
    int CONSTANT_FIELD               = 9;
    int CONSTANT_METHOD              = 10;
    int CONSTANT_INTERFACEMETHOD     = 11;
    int CONSTANT_NAMEANDTYPE         = 12;

    /* Access and modifier flags */
    int ACC_PUBLIC                   = 0x00000001;
    int ACC_PRIVATE                  = 0x00000002;
    int ACC_PROTECTED                = 0x00000004;
    int ACC_STATIC                   = 0x00000008;
    int ACC_FINAL                    = 0x00000010;
    int ACC_SYNCHRONIZED             = 0x00000020;
    int ACC_VOLATILE                 = 0x00000040;
    int ACC_TRANSIENT                = 0x00000080;
    int ACC_NATIVE                   = 0x00000100;
    int ACC_INTERFACE                = 0x00000200;
    int ACC_ABSTRACT                 = 0x00000400;
    int ACC_SUPER                    = 0x00000020;
    int ACC_STRICT		               = 0x00000800;
    int ACC_SYNTHETIC                = 0x00001000;

    /* Access and modifiers - Java 5 */
    int ACC_ANNOTATION               = 0x00002000;
    int ACC_ENUM                     = 0x00004000;
    int ACC_BRIDGE                   = 0x00000040;
    int ACC_VARARGS                  = 0x00000080;
    
    /* Type codes */
    int T_CLASS                      = 0x00000002;
    int T_BOOLEAN                    = 0x00000004;
    int T_CHAR                       = 0x00000005;
    int T_FLOAT                      = 0x00000006;
    int T_DOUBLE                     = 0x00000007;
    int T_BYTE                       = 0x00000008;
    int T_SHORT                      = 0x00000009;
    int T_INT                        = 0x0000000a;
    int T_LONG                       = 0x0000000b;

}
