package servise.ftp

/**
 * Created by 1 on 27.02.2018.
 */
class ClientFtpTest extends GroovyTestCase {
    void testFtpConn() {

       // def patch="c:\\ftconfig.ini"
        ClientFtp.ftpConn("ftp.selcdn.ru","37344_ensit","silesta18");

    }
}
