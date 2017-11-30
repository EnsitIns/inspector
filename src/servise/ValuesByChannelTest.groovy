package servise

import connectdbf.SqlTask

/**
 * Created by 1 on 30.05.2017.
 */
class ValuesByChannelTest extends GroovyTestCase {

    @Override
    void tearDown() {


        def connect = SqlTask.connectCurrent;

        if (connect) {
            SqlTask.connectCurrent.close();
        }

        super.tearDown();
    }

    void setUp() {


        String user_dir = System.getProperty("user.dir");

        def a = user_dir.tokenize('\\');

        def last = a[-1];

        a.remove(last);
        user_dir = a.join('\\');

        def connectionDbf = SqlTask.openTestBase(user_dir, "KlientsBase");

        SqlTask.connectCurrent = connectionDbf;



        super.setUp()

    }

    void testSendSMS() {

    ValuesByChannel channel=new ValuesByChannel(null,null);

        channel.sendSMS("Отправка данных клиентам...","89172562616","40");


    }
}
