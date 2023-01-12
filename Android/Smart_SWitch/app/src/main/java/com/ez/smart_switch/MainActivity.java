package com.ez.smart_switch;
/////////////////////////////////////////////////
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import static android.os.AsyncTask.Status.FINISHED;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{
    private LinearLayout ml;
    private RelativeLayout ml_bar;
    private View item_img = null;
    private ImageView menu;
    private ImageView img_swON;
    private ImageView img_swOFF;
    private ImageView img_swONpress;
    private ImageView img_swOFFpress;
    private ImageView img_swDeactive;
    private TextView room_name;
    private TextView mode_WR;
    private TextView status_net;
    private TextView name_serv;
    private TextView tmr_serv;
    ProgressBar progressBar;
    ProgressBar bar_waitCMD;

    boolean touchFlag = false;
    boolean status_SW = false;
    static boolean data_read = false;
    boolean connect_server = false;
    boolean mRun = true;
    static boolean thr_start = false;
    boolean initESP_ON = false;
    static boolean flag_dialog = false;
    static boolean wait_ENDSENDTCP = false;
    boolean new_link_create = false;

    String tag = "TAG";
    String netSSID_cur = "";
    String [] str_tmp = new String[2];
    String[][] str_setONOF = new String[11][100];   /// массивы для хранения записей настроек,
    String[] str_setONOF_tmp = new String[100];     /// массив для временного хранения записей при редактировании списка
    String  str_dataForDel;                         /// строка подготовленная к удалению, временное хранение
    String str_data = "";                           /// для временных данных
    /*

    str_setONOF[1][100] - настройки режима сутки
    str_setONOF[2][100] - настройки режима по дате
    str_setONOF[4][100] - str_setONOF[10][100] - дни недели ([4]-воскрес. по [10]-субб.)
    * */
    int[] num_str_setONOF = new int[11];      /// массив для сохранения кол-ва записей режимов

    /*
    num_str_setONOF[1] - кол-во для режима сутки
    num_str_setONOF[2] - кол-во для режима по дате
    num_str_setONOF[4]-num_str_setONOF[10] - кол-во для дни недели ([4]-воскрес. по [10]-субб.)
    * */
    static String status_SERV = "status_SERVER";
    private String mServerMessage = "";
    String port, SERVER_IP;
    String cur_data_cl;
    String name_SSID = "";
    String pass_SSID = "";
    String stat_info_prog = "";
    String resiev_IP = "";
    String str_mess = "";
    String set_DAYweek = "";

    static char[] bufTCPout = new char[192];
    static char[] bufTCPin = new char[540]; //new char[192];

    int eX, eY, poseY, width, hight, step;
    int alpha_init = 0;
    int alpha_off = 255;
    static int count_wait_answer = 0;       // счетчик ожидания ответа от сервера
    int port_int;
    static int count_req = 1;
    static int scalegr;
    int count_start_req = 0;
    int count_lost_connect = 0;
    int count_wait_data = 0;
    static int cmd_send;
    static int SECOND_;
    int cnt_0_25_sec = 0;
    int cnt_sys_10sec = 37;             /// счетчик 10сек интервала системного времени
    static int size_bufIN = 0;
    int myHourD = 07;
    int myMinuteD = 00;
    int selYear, selMon, selDay;
    int sel_MODE;                       /// переменная для выбора режима при создании записей вкл/выкл
    int modeWORK_cur;                   /// переменная для установки текущего режима работы (сутки или неделя, по дате работает всегда паралельно текущему режиму)
    int num_dayweek;
    int cnt_answ = 0;
    int sel_MODE_var;           ///
    /////////////////////////////////////////////////КОМАНДЫ
//    static int mode_light = 0x35;               // применить настройки яркости                      ////десятич.с. 53
    final int req_data = 0x34;                  //запрос телеметрии                                 ////десятич.с. 52
    final int set_time_module = 0x35;           //установка времени и даты в модуле

    static int set_link	= 0x11;                 // применить настройки сети
    final int synchro = 0x64;                   // синхронизировать время                           ////десятич.с. 100
    static int ON_SW = 10;                      // включить нагрузку
    static int OFF_SW = 11;                     // выключить нагрузку
    static int addSET_day = 61;                 // добавить запись включения/выключения суточного режима
    static int delSET_day = 62;                 // удалить запись включения/выключения суточного режима
    static int del_ALLSETday = 63;             // удалить все записи суточного режима
    static int addSET_week = 54;                 // добавить запись включения/выключения на один из дней недели (недельный режим)
    static int delSET_week = 55;                 // удалить запись включения/выключения на один из дней недели (недельный режим)
    static int del_ALLSET_week = 56;             // удалить все записи на один из дней недели (недельный режим)
    static int addSET_data = 58;                 // добавить запись включения/выключения по дате
    static int delSET_data = 59;                 // удалить запись включения/выключения по дате
    static int del_ALLSET_data = 60;             // удалить все записи выключения по дате
    static int ON_daymode = 20;                  // включить суточный режим (выключает недельный режим)
    static int ON_weekmode = 21;                 // включить недельный режим (выключает суточный режим)
    static int get_schedule = 33;                 // получить расписание вкл./выкл.
    int mode_synchro = 1;
    long timeMillis;
    final int net_NOT_found = 0;
    final int net_dimmer_NOT_found = 1;
    final int wait_scan_net = 2;
    char mode_serv = '1';
    boolean cmd_OK = false;              // отправленная команда успешно принята устройством
    boolean synchro_OK = false;
/////////////////////////////////////////////////

    static float scale_X;
    static float scale_Y;

    List<String> name_adr;
    int num_save_ip;
    int select_pos;
    int new_select_pos;
    boolean new_IP_found;

    SharedPreferences sPref;
    tcp_client clientTCP;
    Timer timer;
    TimerTask mTimerTask;
    Timer timer2;
    TimerTask timereq2;
    final Context cntx = this;
    Telemetry tele;
    Calendar dateAndTime;
    SimpleAdapter sAdapter;
    ArrayList<Map<String, Object>> data_str;
    Map<String, Object> Obj_m;
    ListView lvSimple;
    BroadcastReceiver wifi_BroadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ml = (LinearLayout) findViewById(R.id.ml);
        ml_bar = (RelativeLayout) findViewById(R.id.ml_bar);
        status_net = (TextView) findViewById(R.id.status_net);
        name_serv = (TextView) findViewById(R.id.name_serv);
        tmr_serv = (TextView) findViewById(R.id.time_serv);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        menu = (ImageView)findViewById(R.id.menu);
        img_swON = (ImageView) findViewById(R.id.sw_ON);
        img_swOFF = (ImageView) findViewById(R.id.sw_OFF);
        img_swOFFpress = (ImageView) findViewById(R.id.sw_ON_press);
        img_swONpress = (ImageView) findViewById(R.id.sw_OFF_press);
        img_swDeactive = (ImageView) findViewById(R.id.sw_deactive);
        room_name = (TextView) findViewById(R.id.room_name);
        mode_WR = (TextView) findViewById(R.id.modeWR);
        menu.setOnTouchListener(this);
        room_name.setOnLongClickListener(click_room_name);
        name_serv.setOnLongClickListener(click_name_serv);
        mode_WR.setOnLongClickListener(click_modeWR);
     //   img_swON.setOnClickListener(click_switch);
     //   img_swOFF.setOnClickListener(click_switch);
        img_swON.setOnTouchListener(this);
        //////////////////////////////////
        // получение ширины текущего разрешения
        WindowManager w = getWindowManager();// объект менеджер окна
        Display d = w.getDefaultDisplay();
        width = d.getWidth();
        hight = d.getHeight();
        step = width/100;                    // вычисление размера 1 процента в пикселях, всего 100(от 5 до 100)
        double tmp_X, tmp_Y;
        tmp_X = (double)width;
        tmp_Y = (double)hight;
        scale_X = (float)(tmp_X/1080);
        scale_Y = (float)(tmp_Y/1920);
        Log.d(tag, "width " + width);
        Log.d(tag, "hight " + hight);
        /////////////////////////////////////
        ml.setBackground(createLayerDrawable(R.drawable.dimmer_fon, 1, 1)); /// установка бэкграунда нужного разрешения
        ml_bar.setBackground(createLayerDrawable(R.drawable.bar_m, 1, (float)0.072)); /// установка бэкграунда нужного разрешения ( по горизонтали вместо 1 было 0,12, размер бэкграунда сохранялся при этом, но картинка сильно искажалась)
        menu.setImageDrawable(createLayerDrawable(R.drawable.menu_pic, (float)0.12, (float)0.067));
        room_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, 80*scale_Y);            // размер текста в пикселях
        status_net.setTextSize(TypedValue.COMPLEX_UNIT_PX, 56*scale_Y);            // размер текста в пикселях
        name_serv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 56*scale_Y);            // размер текста в пикселях
        tmr_serv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60*scale_Y);            // размер текста в пикселях
        mode_WR.setTextSize(TypedValue.COMPLEX_UNIT_PX, 80*scale_Y);            // размер текста в пикселях
/*
        img_swON.setImageDrawable(createLayerDrawable(R.drawable.switch_on, (float)0.75, (float)0.42));
        img_swOFF.setImageDrawable(createLayerDrawable(R.drawable.switch_off, (float)0.75, (float)0.42));
        img_swOFFpress.setImageDrawable(createLayerDrawable(R.drawable.but_swoff_press, (float)0.75, (float)0.42));
        img_swONpress.setImageDrawable(createLayerDrawable(R.drawable.but_swon_press, (float)0.75, (float)0.42));
        img_swDeactive.setImageDrawable(createLayerDrawable(R.drawable.but_swon_deactiv, (float)0.75, (float)0.42));
        */
        img_swON.setImageDrawable(createLayerDrawable(R.drawable.switch_on, (float)0.37, (float)0.21));
        img_swOFF.setImageDrawable(createLayerDrawable(R.drawable.switch_off, (float)0.37, (float)0.21));
        img_swOFFpress.setImageDrawable(createLayerDrawable(R.drawable.but_swoff_press, (float)0.37, (float)0.21));
        img_swONpress.setImageDrawable(createLayerDrawable(R.drawable.but_swon_press, (float)0.37, (float)0.21));
        img_swDeactive.setImageDrawable(createLayerDrawable(R.drawable.but_swon_deactiv, (float)0.37, (float)0.21));

        set_pos_but(menu, 20*scale_X, 5*scale_Y);
        set_pos_but(img_swON, 133*scale_X, 600*scale_Y);
        set_pos_but(img_swOFF, 133*scale_X, 600*scale_Y);
        set_pos_but(img_swOFFpress, 133*scale_X, 600*scale_Y);
        set_pos_but(img_swONpress, 133*scale_X, 600*scale_Y);
        set_pos_but(img_swDeactive, 133*scale_X, 600*scale_Y);

        image_ON_OFF(false);
        set_pos_but(room_name, 120*scale_X, 120*scale_Y);
        set_pos_but(mode_WR, 120*scale_X, 300*scale_Y);
        set_pos_but(status_net, 120*scale_X, 140*scale_Y);
        set_pos_but(progressBar, 800*scale_X, 140*scale_Y);
        set_pos_but(name_serv, 120*scale_X, 240*scale_Y);
        set_pos_but(tmr_serv, 120*scale_X, 350*scale_Y);
      //  set_pos_but(menu, 1*scale_X, 1*scale_Y);

        ///////////////////////////инициализация подключения TCP ip
        SERVER_IP = read_config_str("server_IP");
        port = read_config_str("server_port");
        select_pos = read_config_int("select_pos");
        num_save_ip = read_config_int("num_save_ip");           // читаем кол-во сохраненных IP
        if(select_pos == 0)select_pos = 1;
        if(SERVER_IP.equals("")){SERVER_IP = "192.168.4.1";}
        port = "8888"; // port = "8888" для диммера и розетки и "8558" - для термостата;

        port_int = Integer.parseInt(port);
        clientTCP = new tcp_client(SERVER_IP, port_int);

        if(read_config_str("new_name_obj"+select_pos).equals("")){ room_name.setText("Объект "+select_pos);}
        else{ room_name.setText(read_config_str("new_name_obj"+select_pos)); }
        Log.d(tag, " room_name "+room_name);   //
        cur_data_cl = cur_data();
//        data_str = new ArrayList<Map<String, Object>>( num_str_setONOF[sel_MODE] ); //// объявляем массив строк размерностью num_str_setONOF[var_selmode]
        load_data_strONOF();
        Log.d(tag, "sel_MODE_var "+sel_MODE_var);
/*
        for(int k = 0; k<11; k++){ num_str_setONOF[k] = 0; }
        str_setONOF[1][0] ="1DAY1"; str_setONOF[1][1] ="0DAY2"; str_setONOF[1][2] ="1DAY3"; num_str_setONOF[1] = 3;
        str_setONOF[2][0] ="1Date1"; str_setONOF[2][1] ="0Date2"; str_setONOF[2][2] ="0Date3"; str_setONOF[2][3] ="1Date3"; num_str_setONOF[2] = 4;
        str_setONOF[4][0] ="0Week1_1"; str_setONOF[4][1] ="1Week1_2"; num_str_setONOF[4] = 2;
        str_setONOF[5][0] ="1Week2_1"; str_setONOF[5][1] ="0Week2_2"; str_setONOF[5][2] ="1Week2_3"; num_str_setONOF[5] = 3;
        str_setONOF[6][0] ="1Week3_1"; str_setONOF[6][1] ="0Week3_2"; num_str_setONOF[6] = 2;
*/
        ///////////////////Для получения инфо о подключенной сети WIFI
        wifi_BroadcastReceiver = new WIFI_BroadcastReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);   //.NETWORK_STATE_CHANGED_ACTION
        this.registerReceiver(wifi_BroadcastReceiver, filter);
        //////////////////////////////////////////

//        get_name_ssid();                // получаем имя сети
//        wifi_reconnect_net(this, "Smart_Home_EZ", "12345678");
        /////////////////////////////////////
        // обработка события касания экрана ..........
        View root = findViewById(android.R.id.content).getRootView();
        root.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (touchFlag) {
                    System.err.println("Display If  Part ::->" + touchFlag);

                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.d(tag, "onTouch DOWN oncreate");
                            eY = (int) event.getY();
                            poseY = eY;
                            if(item_img.getId() == R.id.menu){
                                Log.d(tag, "PUSH menu!!!");
                                showPopupMenu(item_img);                /// вывод меню
                            }
                            if(item_img.getId() == R.id.sw_ON){
                                if(status_SW){
                                    image_ON_OFF(false);
                                    img_swONpress.setImageAlpha(alpha_off);
                                }else{
                                    image_ON_OFF(true);
                                    img_swOFFpress.setImageAlpha(alpha_off);
                                }
                                img_swOFF.setImageAlpha(alpha_init);
                                img_swON.setImageAlpha(alpha_init);
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            eX = (int) event.getX();
                            eY = (int) event.getY();
                            //if(item_img.getId() != R.id.menu)cmd_send = mode_light;
                            break;
                        case MotionEvent.ACTION_UP:
                            Log.d(tag, "onTouch UP oncreate");
                            if(item_img.getId() != R.id.menu) {

                                //cmd_send = mode_light; wait_ENDSENDTCP = true;
                                Log.d(tag, "room_name.getX() " + room_name.getX()+" room_name.getY() "+room_name.getY());
                            }
                            if(item_img.getId() == R.id.sw_ON){
                                flag_dialog = true;
                                if(status_SW){
                                    status_SW = false;
                                    image_ON_OFF(false);
                                    cmd_send = OFF_SW;
                                }else{
                                    status_SW = true;
                                    image_ON_OFF(true);
                                    cmd_send = ON_SW;
                                }
                                handle_funcONOF(status_SW);
                            }
                            touchFlag = false;
                            break;
                    }
                }
                return true;
            }
        });
        ///////////////////////////////////////////

        //////////////////////////////////таймер, работает с TCP соединением
        timer = new Timer();
        mTimerTask = new MyTimerTask();
        try{timer.schedule(mTimerTask, 250, 250);}catch( Exception c){ Log.d(tag, "Exception "+ c); }

        timer2 = new Timer();
        timereq2 = new TimerTask2();
        if(read_config_int("saved_show_help") ==0 ){try{timer2.schedule(timereq2, 2000);}catch(Exception cx){;} } // одноразовый запуск таймера через 2сек

        //command = req_data_serv;
        cmd_send = req_data;            // установка команды на запрос телеметрии
        for(int a = 0; a< bufTCPout.length; a++){ bufTCPout[a] = '_'; }
 //       cmd_send = synchro;
        name_serv.setText("Сервер "+SERVER_IP);

        //////////////////////////////////////////
    }
    ///////////////////END ON CREATE///////////
    ///////////////////////////////////////////
    //////////////////////////////////////////
    void load_data_strONOF(){
        /////// заполнение массивов сохраненных записей
        for(int k = 1; k<10; k++) {
            sel_MODE_var = k;
            int cnt_wr = read_config_str(SERVER_IP + sel_MODE_var, str_setONOF_tmp);
            num_str_setONOF[k] = cnt_wr;
            for (int i = 0; i < cnt_wr; i++) {
                str_setONOF[k][i] = str_setONOF_tmp[i];
//                Log.d(tag, "str "+i+": "+str_setONOF_tmp[i]+"\n");
            }

        }

    }
    ///////////////////////////////////////////
    void handle_funcONOF(boolean stat){     /// обработка после нажатия кнопки вкл/выкл

        if(!stat) {
            int cnt_wr = read_config_str(SERVER_IP, str_setONOF_tmp);
            Log.d(tag, "press_but " + SERVER_IP + ", cnt_wr = " + cnt_wr);
            for (int i = 0; i < cnt_wr; i++) {
                 Log.d(tag, "press_but, i= " + i + ", data " + str_setONOF_tmp[i] + "\n");
            }
        }else{

        }

    }
    public class WIFI_BroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        NetworkInfo nwInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        nwInfo.getState();

            WifiManager wifiManager = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();

            List<WifiConfiguration> listOfConfigurations = wifiManager.getConfiguredNetworks();
            for (int index = 0; index < listOfConfigurations.size(); index++) {
                WifiConfiguration configuration = listOfConfigurations.get(index);
                if (configuration.networkId == info.getNetworkId()) {
                    netSSID_cur = configuration.SSID;
                    netSSID_cur = netSSID_cur.replace("\"", "");
                    //return configuration.SSID;
                    Log.d(tag, ", ssid "+netSSID_cur);
                    //return ssid;
                }
            }
        }

    }
    /* работает только до андр.7 версии
    private String getCurrentSsid(Context context) {
        String ssid = null;

        int netID;
        try {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo.isConnected()) {
                final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//                wifiManager.startScan();
                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                if (connectionInfo != null) {
                    ssid = connectionInfo.getSSID();  netID = connectionInfo.getNetworkId();
                    Log.d(tag, "netID = "+ netID+", ssid "+ssid);

                }
            }
        }catch(Exception e){ Log.d(tag, "Exception getCurrentSsid!"); }
        return ssid;

////////////////////////
        return null;
     }
     */
    //////////////////////////////////////////
    /* работает только до андр.7 версии
    String get_name_ssid(){
        String name = "";
        netSSID_cur = getCurrentSsid(this);                                     // получаем имя сети
//        Log.d(tag, "netSSID_cur "+netSSID_cur);
        try {
            char[] tmp_char = new char[netSSID_cur.length() - 2];                   // объявляем массив символов длиной netSSID_cur.length()-2
            netSSID_cur.getChars(1, netSSID_cur.length() - 1, tmp_char, 0);           // копируем имя сети в массив символов исключая 1 и последний символы(кавычки)
            String tmp_str = new String(tmp_char);                                  // инициализируем новую строку элементами массива символов
            netSSID_cur = tmp_str;
            name = netSSID_cur;
            Log.d(tag, "name SSID " + netSSID_cur);
        }catch(Exception tt){  Log.d(tag, "NOt connection WIFI! "); }
        return name;
    }
    */
    //////////////////////////////////////////
    private Drawable createLayerDrawable(int ID_drw, float x, float y) {     //получаем объект Drawable из ресурсов (id = "ID_drw") нужной ширины "x"  и высоты "y"
        float xx = (float)width*x;
        float yy = (float)hight*y;
        Bitmap bitm = BitmapFactory.decodeResource(getResources(), ID_drw);
        Bitmap btm = bitm.createScaledBitmap(bitm, (int)xx, (int)yy, true);
        BitmapDrawable drawable0 = new BitmapDrawable(getResources(), btm);
//    BitmapDrawable drawable0 = (BitmapDrawable) getResources().getDrawable(
//            R.drawable.bg_main1920);
        Log.d(tag, "widht "+btm.getWidth()+" hight "+btm.getHeight());

        return drawable0;
    }
    //////////////////////////////////////////////////////////
    void set_pos_but(View v, float x, float y){                 // если не выполнить эту функцию сначала, то функция v.set.X(float x) работает некорректно
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(

                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins((int)x,(int)y,0,0);
        //                       lp.setMargins(x, y, 0, 0);
        v.setLayoutParams(lp);
    }
    ///////////////////////////////////////////////////////////////////////////
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchFlag = true;
                item_img = v;
                Log.d(tag, "onTouch DOWN");
                break;
            case MotionEvent.ACTION_UP:
                touchFlag = false;
                Log.d(tag, "onTouch UP");
                break;
            default:
                break;
        }
        return false;
    }
    //////////////////////////////////////////////////////////
    void image_ON_OFF(boolean ON){
        if(ON) {
            img_swOFF.setImageAlpha(alpha_init);
            img_swON.setImageAlpha(alpha_off);
//            cmd_send = ON_SW;
        }else{
            img_swOFF.setImageAlpha(alpha_off);
            img_swON.setImageAlpha(alpha_init);
//            cmd_send = OFF_SW;

        }
        //////////////

        ///////////////
        img_swDeactive.setImageAlpha(alpha_init);
        img_swOFFpress.setImageAlpha(alpha_init);
        img_swONpress.setImageAlpha(alpha_init);
        /////////////
 //               show_txt_toast("Pressed Button!");
        /////////////
    }
    //////////////////////////////////////////
    View.OnLongClickListener click_room_name = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            dialog_show(6);
            return false;
        }
    };
    //////////////////////////////////////////
    View.OnLongClickListener click_name_serv = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            dialog_show(5);
            return false;
        }
    };
    View.OnLongClickListener click_modeWR = new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            flag_dialog = true;
            if(modeWORK_cur == 0){
                mode_WR.setText("Режим 'Неделя'");
                mode_WR.setTextColor(getResources().getColor(R.color.work_W));
                modeWORK_cur = 1;
                cmd_send = ON_weekmode;
            }
            else{
                mode_WR.setText("Режим 'Сутки'");
                mode_WR.setTextColor(getResources().getColor(R.color.work_D));
                modeWORK_cur = 0;
                cmd_send = ON_daymode;
            }

            return false;
        }
    };

    /*
    View.OnClickListener click_switch = new View.OnClickListener(){
        public void  onClick(View v) {
            //           dialog_show(5);
            if(status_SW){
                status_SW = false;
                image_ON_OFF(false);
            }else{
                status_SW = true;
                image_ON_OFF(true);
            }
            Log.d(tag, "press_but");
        }
    };
    */
    /////////////////////////////////////////////////////////////////////
    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu); // Для Android 4.0
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menu_1:                       /// Настройка устройства
                        dialog_show(1);
                        break;
                    case R.id.menu_2:                       /// Режим "Сутки"
                        dialog_show(8);
                        break;
                    case R.id.menu_3:                       /// Режим "Неделя"
                        dialog_show(9);
                        break;
                    case R.id.menu_4:                       /// Режим "Вкл. по дате"
////////////////////////////////////////
                        dialog_show(10); /// debug dialog_show(3);
 ///////////////////////////////////////
                        break;
                    case R.id.menu_5:                       /// О приложении
                        dialog_show(2);
                        break;
                    case R.id.menu_6:                       /// Помощь
                        dialog_show(4);
                        break;
                    case R.id.menu_7:                       /// Выход
                        close_TCP();
                        finish();
                        break;
                    case R.id.menu_8:                       /// Синхронизация расписания с модулем
                        dialog_show(11);
                        break;
                    case R.id.menu_9:                       /// Синхронизация времени между андроид и модулем
                        dialog_show(12);
                        break;
                    case R.id.menu_10:
                        dialog_show(13);
                        break;
                }
                return true;
            }
        });
        MenuPopupHelper menuHelper = new MenuPopupHelper(this, (MenuBuilder) popupMenu.getMenu(), v);
        menuHelper.setForceShowIcon(true);
        menuHelper.setGravity(Gravity.END); menuHelper.show();

    }
    ///////////////////////отображение статуса подключения......
    Handler handlstatus = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            status_net.setText(status_SERV);
            if (status_SERV.equals("подключаюсь..")) {
                count_lost_connect++;
                if (count_lost_connect > 8) {
                    progressBar.setVisibility(View.VISIBLE); // прогрессбар крутится
                }
                if (count_lost_connect > 1000) count_lost_connect = 0;
            } else {
                count_lost_connect = 0;
                progressBar.setVisibility(View.GONE);                                       // прогрессбар  невидим
            }
        }
    };
    ////////////////////////вывод всплывающих сообщений
    void show_txt_toast(String str){
        Toast.makeText(this,str, Toast.LENGTH_SHORT).show();
    }
    /////////////////////////////////////////////
    void func_req_data(int command){
        int [] dim = new int[64];
//        if(command == synchro)count_req++;
        if (count_req > 255) count_req = 1;
        Log.d(tag, "Send_comand, count REQ; " + count_req+", command: "+cmd_send+" scalegr "+scalegr);
//////////////////////////////////////////////////////////////
        Send_com send = new Send_com(bufTCPout, dim, command);

        Log.d(tag, "Send_comand = "+(int)bufTCPout[68]);
        str_mess = new String(bufTCPout);    // копирование массива символов в строку
//        clientTCP.send_m(str_mess);
        new Thread(new Runnable() {         // Если запускать без потока, то работает не на всех телефонах
            @Override
            public void run() {
                clientTCP.send_m(str_mess);
            }}).start();
//    try { Thread.sleep(100); } catch (Exception ex_cr) { Log.d(tag, "Exception ex_cr"); }
    }
    ///////////////////////////////////////
    int find_SECOND_(String str_time){          // получение системного времени в секундах
        int sec = 0;
        String hour, min, SEC_;
        char [] tmr= new char[8];
        try{
            str_time.getChars(11, 19, tmr, 0);
            hour = ""+tmr[0]+tmr[1];
            min = ""+tmr[3]+tmr[4];
            SEC_ = ""+ tmr[6]+tmr[7]; //sec = 352;
            sec = (Integer.parseInt(hour))*60*60+Integer.parseInt(min)*60+Integer.parseInt(SEC_);
//            Log.d(tag, "hour  :: "+Integer.parseInt(hour)+", min :: "+Integer.parseInt(min)+", sec :: "+Integer.parseInt(SEC_));
        }
        catch(Exception e){Log.d(tag, "SECOND_ ");}
        return sec;
    }
    void close_TCP(){
        clientTCP.tcp_close();
        count_wait_data = 45;       /// при переключении между устройствами чтобы не ждать 12 сек для перезапуска клиента
        mRun = false;

    }
    int synchro(int mode){                          /// получает режим для синхронизации от 1 до 9, возвращает номер следующего режима до 10. Когда возвращает 10 - синхронизация окончена
        int next_mode = 0;
        ///////стирание списка///////
        //Log.d(tag, "convert "+data_for_SCREEN(1, "1:26.2.0"));              /// конвертирование записи формата модуля в формат для вывода на экран
        sel_MODE_var = mode;
        if(sel_MODE_var>2)sel_MODE_var++;
        int cnt_wr = read_config_str(SERVER_IP + sel_MODE_var, str_setONOF_tmp);  /// получаем кол-во записей для этогоIP и соответствующего режима
        //num_str_setONOF[sel_MODE_var] = cnt_wr;
        for(int i = 0; i<cnt_wr; i++) str_setONOF_tmp[i] = "";
        num_str_setONOF[sel_MODE_var] = 0;
        saved_config(SERVER_IP+sel_MODE_var, str_setONOF_tmp, num_str_setONOF[sel_MODE_var]);   // стираем список
        ///////запись полученных значений///////////
        String getstr = ""; int cntsymb = 0; int cur_pos = 37;
        for(int i = 37; bufTCPin[i]!= '!'; i++){ getstr = getstr + bufTCPin[i]; cur_pos++; }   /// получаем кол-во записей в модуле
//                    Log.d(tag, "getstr "+getstr);
        cntsymb = Integer.parseInt(getstr);
//                    Log.d(tag, "cntstr "+cntstr+" cur_pos "+cur_pos);
        getstr = ""; cur_pos++;
        int cntstr = 0;
        for(int i = cur_pos; bufTCPin[i]!= '!'; i++){
            if(bufTCPin[i]!='\n'){ getstr = getstr + bufTCPin[i]; }
            else{
                str_setONOF_tmp[cntstr] = data_for_SCREEN(sel_MODE_var, getstr);
                Log.d(tag, "getstr "+str_setONOF_tmp[cntstr] + " cntstr = "+cntstr);
                getstr = ""; cntstr++;
            }

        }
        num_str_setONOF[sel_MODE_var] = cntstr;
        Log.d(tag, "sel_MODE_var "+sel_MODE_var + " num_str_setONOF[sel_MODE_var] = "+num_str_setONOF[sel_MODE_var]);
        saved_config(SERVER_IP+sel_MODE_var, str_setONOF_tmp, num_str_setONOF[sel_MODE_var]);   // сохраняем список
        next_mode = mode +1;
        return next_mode;
    }
    void pars_data(String str){
///////////////////////////////////
// команда отправлена 68 байтом, но после перезагрузки димер отправляет её  36 байтом
//////////////////////////////////
        int num_r;

        if(size_bufIN > 38) {
            try {
                /*
                str.getChars(0, bufTCPin.length, bufTCPin, 0); // копирование символов строки в массив bufTCPin
                num_r = ((int) bufTCPin[46]) | (((int) bufTCPin[47]) << 8);  // получаем номер последнего запроса
                tele = new Telemetry(bufTCPin, count_req);
                if (cmd_send != req_data) {                  // если была отправлена какая-то команда, отличная от запроса телеметрии, то ждем подтверждения получения ее от сервера и дублируем запросы этой же командой
                    if (cmd_send == tele.COMAND && count_req == tele.count_COMAND) {
                        Log.d(tag, "cmd_send " + cmd_send + ", count_req " + count_req + " tele count_req " + tele.count_COMAND);
                    }
                }
///////////////////////////////////////
                String sstt = "", sst_p = "";
                for (int y = 0; y < 12; y++) {
                    sstt = sstt + bufTCPin[y + 19];
                }
                for (int y = 0; y < 4; y++) {
                    sst_p = sst_p + bufTCPin[y + 31];
                }
                Log.d(tag, "IP_adr " + sstt + "  " + "port_N  " + sst_p);
///////////////////////////////////////

                count_wait_answer = 0;                  // в случае успешного приема сбрасываем счетчик ожидания ответа
                */
                str.getChars(0, size_bufIN, bufTCPin, 0); // копирование символов строки в массив bufTCPin
                if(bufTCPin[36] == req_data){       /// отработка получения статуса модуля
                    String tmr_str = new String(bufTCPin);
                    tmr_str = tmr_str.substring(37, 53);
                    Log.d(tag, "Принято - команда: " + Integer.toString(bufTCPin[36])+ "__"+ bufTCPin[57]+ "__"+ bufTCPin[59]); // bufTCPin[57] - статус вкл/выкл("1"/"0"), bufTCPin[59] - режим работы сутки/неделя("0"/"1")
                    if(bufTCPin[56] == '!'){
                        if(bufTCPin[57] == '1'){ image_ON_OFF(true); }
                        else{ image_ON_OFF(false); }
                        tmr_str = "Дата и время сервера\n" + tmr_str;
                        tmr_serv.setText(tmr_str);
                    }
                    if(bufTCPin[58]=='!'){
                        if(bufTCPin[59]=='1'){
                            mode_WR.setText("Режим 'Неделя'");
                            mode_WR.setTextColor(getResources().getColor(R.color.work_W));
                            modeWORK_cur = 1;
                        }
                        else{
                            mode_WR.setText("Режим 'Сутки'");
                            mode_WR.setTextColor(getResources().getColor(R.color.work_D));
                            modeWORK_cur = 0;
                        }
                    }
                }
                if (bufTCPin[36] == get_schedule) {
                    String str_fl = "";
                    if(synchro(mode_synchro)<10) {
                        flag_dialog = true;
                        mode_synchro++;
                        if(mode_synchro == 2){ str_fl = "value_schedule.lua"; }
                        else{ str_fl = "valueweek_day"+(mode_synchro-2)+".lua";}
                        str_fl.getChars(0, 18, bufTCPout, 69);
                        cmd_send = get_schedule;
                        count_req++;
                    }
                    else {
                        synchro_OK = false;
                        mode_synchro = 1;
                        flag_dialog = false;
                        ////////////////////////////////////////////
                        cmd_send = req_data;   // команда управления прошла успешно, в переменную command помещаем команду запроса телеметрии
                        count_req++;
                        show_txt_toast("Синхронизировано!");
                    }
                    load_data_strONOF();
                }
            } catch (Exception e) {
                Log.d(tag, "Exception pars_data client func");
            }
        }else{
            try{
                ////////////// в штатном режиме модуль отправляет по 38 байт, ниже обработка
                str.getChars(0, size_bufIN, bufTCPin, 0); // копирование символов строки в массив bufTCPin
/*                if(bufTCPin[36] == mode_light || bufTCPin[36] == synchro){
                    cmd_send = synchro;                                    // debug tmp
                }*/
                Log.d(tag, "Принято - команда: " + Integer.toString(bufTCPin[36])+" номер запроса "+Integer.toString(bufTCPin[37]));
 //////////////////////////////////////////////////
                if (bufTCPin[36] == set_link) {
                    if(!new_link_create) {
                        String sstt = "", sst_p = "";
                        for (int y = 0; y < 12; y++) {
                            sstt = sstt + bufTCPin[y + 19];
                        }
                        for (int y = 0; y < 4; y++) {
                            sst_p = sst_p + bufTCPin[y + 31];
                        }
                        Log.d(tag, "IP_adr " + sstt + "  " + "port_N  " + sst_p);
                        SERVER_IP = "" + bufTCPin[19] + bufTCPin[20] + bufTCPin[21] + "." + bufTCPin[22] + bufTCPin[23] + bufTCPin[24] + "." + bufTCPin[25] + bufTCPin[26] + bufTCPin[27] + "." + bufTCPin[28] + bufTCPin[29] + bufTCPin[30];
                        port = "" + bufTCPin[31] + bufTCPin[32] + bufTCPin[33] + bufTCPin[34];
                        initESP_ON = false;
                        Log.d(tag, "SERVER_IP " + SERVER_IP + " PORT_SERV " + port);
                        num_save_ip++;
                        saved_config("num_save_ip", num_save_ip);                               // сохраняем количество известных IP адресов num_save_ip
                        select_pos = num_save_ip; saved_config("select_pos", select_pos);           // текущая позиция из списка
                        saved_config("server_IP"+Integer.toString(num_save_ip), SERVER_IP);     // сохраняем новое устройство в списке IP адресов
                        ////////////////////////////////
                        ////////////
                        //cmd_send = req_data;
                        new_link_create = true;
                    }
                }
                if((bufTCPin[36] == cmd_send)){
                    if((cmd_send == addSET_day || cmd_send == delSET_day || cmd_send == del_ALLSETday) && count_req == bufTCPin[37]){
                        cmd_OK = true;
 //                       Log.d(tag,"cmd_send__ = "+cmd_send+" count_req = "+count_req+" " +Integer.toString(bufTCPin[37])+ " cmd_OK "+cmd_OK);
                    }
                    if(cmd_send != req_data)show_txt_toast("Выполнено!");
                    flag_dialog = false;
                    cmd_send = req_data;   // команда управления прошла успешно, в переменную command помещаем команду запроса телеметрии
                    count_req++;
///                 Log.d(tag,"buf 36 = "+Integer.toString(bufTCPin[36])+" buf 37 = "+Integer.toString(bufTCPin[37]));
                }
//////////////////////////////////////////////////


            }catch(Exception tt){Log.d(tag, "Exception pars_data client func_2");}
        }
        String ip_a= ""; String p = "";
        ip_a = "" + bufTCPin[19] + bufTCPin[20] + bufTCPin[21] + "." + bufTCPin[22] + bufTCPin[23] + bufTCPin[24] + "." + bufTCPin[25] + bufTCPin[26] + bufTCPin[27] + "." + bufTCPin[28] + bufTCPin[29] + bufTCPin[30];
        p = "" + bufTCPin[31] + bufTCPin[32] + bufTCPin[33] + bufTCPin[34];
        Log.d(tag, "IP "+gotIP(ip_a)+" port "+p);
////////////////////////////////////
        count_wait_answer = 0;

    }
    /////////////////////////timer/////////////////////////////
    class MyTimerTask extends TimerTask {                                       // таймер 250 мс
        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    "dd:MM:yyyy HH:mm:ss", Locale.getDefault());
            final String strDate = simpleDateFormat.format(calendar.getTime());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clientTCP.send_staus_socket(str_tmp);
                    status_SERV = str_tmp[0];
                    if(status_SERV.equals("Соединение установлено")){connect_server = true;}
                    else {connect_server = false;}
                    if(status_SERV.equals("Соединение закрыто")){
                        mRun = false;
                    }
   //  Log.d(tag, "status_SERV "+status_SERV);
                    if(data_read){
                        mServerMessage = str_tmp[1];
                        pars_data(mServerMessage);
                        data_read = false;
                        count_wait_data = 0;
                    }
                    else{
                        /*
                        if(clientTCP.tcp_NET.getStatus().equals(FINISHED)){
                            clientTCP = new tcp_client(SERVER_IP, port_int);
                            Log.d(tag, "Сервер перезапущен!");
                        }*/
                        count_wait_data++;
                        if(count_wait_data>44) {                ///более 12 сек ничего не приходит от сервера - перезапускаем соединение
                            close_TCP(); count_wait_data = 0;
                            clientTCP = new tcp_client(SERVER_IP, port_int);
                            Log.d(tag, "status net Клиент перезапущен!");
                        }
///                        Log.d(tag, "status net "+clientTCP.socket.isConnected()+"__"+clientTCP.tcp_NET.getStatus());
                    }
                    handlstatus.sendEmptyMessage(0);
                    count_start_req++;
/////////////////////////////////////////////////////////////////
////       Каждую секунду, в случае не получения подтверждения на отпрвленную команду, дублируется посылка.
/////////////////////////////////////////////////////////////////
                    if(count_start_req == 12) {                       // каждые 3 секунды запрашивается ответ, если нет касания экрана и не получен ответ на предыдущий запрос
                        // if (!touchFlag && count_wait_answer == 0){
                        if (!touchFlag ){
                            //  if (!touchFlag ){
                            //if(cmd_send != req_data && cmd_send != mode_light) {
                            if(cmd_send != req_data) {
//                                if (connect_server) {
                                Log.d(tag, "cmd_send1 "+cmd_send);
                                func_req_data(cmd_send);      // если команда не запрос телеметрии, то отправка команды серверу
//                                } else { cmd_send = req_data; }
                            }
///////////////////////////////////////
//                            cmd_send = req_data;   //for debug
////////////////////////////////////////////
                        }
                        count_start_req = 0;                // сброс счетчика задающего интервал отправки запроса
//                    count_wait_answer++;                // счетчик секунд
                        SECOND_ = find_SECOND_(strDate);
                        //                Log.d(tag, " Second "+ SECOND_);
                    }
                    if(cnt_sys_10sec>40){             /// отсчет 10сек интервала
                        if(cmd_send == req_data && flag_dialog == false) {
                            Log.d(tag, "запрос статуса "+cmd_send);
                            if(!touchFlag){
                                func_req_data(cmd_send);      // раз в 10сек запрос статуса(время модуля и состояние вкл/выкл)
                                count_req++;
                            }

                        }
                        cnt_sys_10sec = 0;
                    }
                    /////////////////////////////////////////////
                    /*
                    if(cmd_send == mode_light) {
                        func_req_data(cmd_send);      //
                        Log.d(tag, "cmd_send2 ");
                        if(wait_ENDSENDTCP){
                            cmd_send = synchro;     // debug tmp
//                             cmd_send = req_data; // debug tmp
                            wait_ENDSENDTCP = false;
                        }
                    }*/
                    /////////////////////////////////////////////
                    cnt_answ++;
                    if(cnt_answ>1000)cnt_answ = 0;
                }
            });
            cnt_0_25_sec++;
            cnt_sys_10sec++;       //// считает тики для 10сек интервала
            if(cnt_0_25_sec>2400)cnt_0_25_sec = 0; /// больше 10 мин - сброс
        }
    }
    //////////////////////////////////////////////////////////
    /////////////////////сохранение и чтение сетевых настроек.......
    void saved_config(String str, int par){
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(str, par);
        ed.commit();
    }
    void saved_config(String str, String par){
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(str, par);
        ed.commit();
    }
    String read_config_str(String str) {
        String tmp = "";
        sPref = getPreferences(MODE_PRIVATE);
        tmp = sPref.getString(str, "");
        return tmp;
    }
    int read_config_int(String str){
        int tmp = 0;
        sPref = getPreferences(MODE_PRIVATE);
        tmp = sPref.getInt(str, 0);
        return tmp;
    }

    void saved_config(String keyStr, String[] dim_str, int num_str){  //// сохранение массива строк dim_str с ключем keyStr
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        Set<String> newstr = new HashSet<String>();
        for(int i = 0; i<num_str; i++){ newstr.add(dim_str[i]); }
        ed.putStringSet(keyStr, newstr);
        ed.apply();
    }
    int read_config_str(String keyStr, String[] dim_str)    //// чтение массива строк dim_str с ключем keyStr
    {
        sPref = getPreferences(MODE_PRIVATE);
        Set<String> ret = sPref.getStringSet(keyStr, new HashSet<String>());
        int i = 0;  //ret.size();
        for(String r : ret) {
            dim_str[i] = r;  i++;            /// заполняет массив, но не в порядке произведенной записи
        }
        //for(int k = 0; k<i; k++){ Log.d(tag, "ret.size() "+ret.size()); } /// /// заполняет массив в порядке произведенной записи, но после запуска приложения не работает
        //Log.d(tag, "ret.size() "+i);
        return i;
    }
    ////////////////////////////////////////////////////////
    void dialog_show(int dialog) {
//        flag_dialog = true;
        final AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Log.d(tag, "Start_dialog 1");
        switch (dialog) {
            case 1:                                                             ///// Настройка устройства
                LinearLayout dev_conf;
                dev_conf = (LinearLayout) getLayoutInflater().inflate(R.layout.device_config, null);
                final EditText login_ssid = dev_conf.findViewById(R.id.config_SSID);
                final EditText pass_ssid = dev_conf.findViewById(R.id.config_pasw);
                final Button apply_conf = dev_conf.findViewById(R.id.button2);
                final ProgressBar progrbar = dev_conf.findViewById(R.id.progrBar);
                final TextView info_prog = dev_conf.findViewById(R.id.info_prog);
                //////////////////////DEBUG////////
                //get_name_ssid();
                //login_ssid.setText(netSSID_cur);
                ///////////////////////////////////
                builder.setView(dev_conf);
                alert = builder.create();
                alert.show();

                if(new_link_create){ show_txt_toast("Перезагрузите приложение!");}
                if(SERVER_IP.equals("192.168.4.1")){ ; }
                else{ close_TCP(); SERVER_IP = "192.168.4.1"; name_serv.setText("IP server "+SERVER_IP); }
                //////////////////////////////////
                final Handler info_NET = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        String mes = "";
                        switch(msg.what){
                            case net_NOT_found:
                                mes = "Нет подключения к сети WIFI!";
                                break;
                            case net_dimmer_NOT_found:
                                //    mes = "Сеть \"Dimmer_EZ\" не найдена!";
                                mes = "Сеть \"Smart_Home_EZ\" не найдена!";
                                break;
                            case wait_scan_net:
                                mes = "Секундочку...";
                                break;
                        }
                        show_txt_toast(mes);
                    }
                };
                //////////////////////////////////
                //get_name_ssid();
                if(netSSID_cur != null ){                              // проверяем, если подключение WIFI
                    login_ssid.setText(netSSID_cur);
                    //               screen_mes_wait.sendEmptyMessage(0);
                    info_NET.sendEmptyMessage(wait_scan_net);
                    // if(netSSID_cur.equals("Dimmer_EZ")){ ; }            // проверяем, если подключение к сети Dimmer_EZ. Если да, то ничего не делаем
                    if(netSSID_cur.equals("Smart_Home_EZ")){ ; }            // проверяем, если подключение к сети Smart_Home_EZ. Если да, то ничего не делаем
                    else {                                              // подключения к сети нет
                        login_ssid.setText(netSSID_cur);
                        wifi_reconnect_net(this, "Smart_Home_EZ", "12345678");
                        //wifi_reconnect_net(this, "Smart_Home_EZ", "open");
     /*                   close_TCP();
                        clientTCP = new tcp_client(SERVER_IP, port_int);*/


 //                       try { Thread.sleep(1500); } catch (Exception ex_cr) { Log.d(tag, "Exception ex_cr"); }  // попытка повторного подключения и пауза 1,5сек
                        //////////////////////////
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String sr = "";
                                boolean sr_OK = false;
/*
                                while (!sr_OK) {
                                    if (sr != null) {
                                        //if (sr.equals("\"Dimmer_EZ\"")) {
                                        //if (sr.equals("\"Smart_Home_EZ\"")) {
                                        if (sr.equals("Smart_Home_EZ")) {
                                            sr_OK = true;
                                        }
                                        Log.d(tag, ", ssid sr = "+sr);
                                    }
                                    try {
                                        Thread.sleep(200);
                                        count_cr++;
                                    } catch (Exception ex_cr) {
                                        Log.d(tag, "Exception ex_cr");
                                    }
                                    if (count_cr == 13) {                   // через 2,5 сек попытка подключиться к сети Termostat_EZAP
                                        // wifi_reconnect_net(cntx, "Dimmer_EZ", "12345678");
                                        wifi_reconnect_net(cntx, "Smart_Home_EZ", "12345678");
                                    }
                                    if (count_cr > 40) {                    // если через 8 сек сеть не найдена выходим из цикла и закрываем диалог
                                        break;
                                    }
                                    //sr = getCurrentSsid(cntx);
                                    sr = netSSID_cur;
                                    Log.d(tag, "Net ==== " + sr+ "count_cr = "+ count_cr);
                                }

*/
                                cnt_0_25_sec = 0;
                                while(!netSSID_cur.equals("Smart_Home_EZ")){
                                    if(cnt_0_25_sec>1200){                                          //// 5 мин на попытку подключения
                                        SERVER_IP = read_config_str("server_IP");
                                        info_NET.sendEmptyMessage(net_dimmer_NOT_found);
                                        alert.dismiss();
                                        break;
                                    }
                                    if(cnt_0_25_sec%40 == 0){                                       /// раз в 10 сек попытка переподключения
                                        if(!sr_OK) {
                                            wifi_reconnect_net(cntx, "Smart_Home_EZ", "12345678");
                                            Log.d(tag, "reconnect..." + netSSID_cur);
                                            info_NET.sendEmptyMessage(wait_scan_net);
                                            sr_OK = true;
                                        }
                                    }else{ sr_OK = false;}
                                }
                                Log.d(tag, "reconnect...cnt_0_25_sec ="+cnt_0_25_sec);
                            }
                        }).start();

                        //////////////////////////
                    }
                }
                else{
                    info_NET.sendEmptyMessage(net_NOT_found);
                    alert.dismiss();
                }

                final Handler handlinfo_startESP = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        info_prog.setText(stat_info_prog);
                    }
                };
                View.OnClickListener apply_config_handle = new View.OnClickListener() {           // отработка пункта меню применить настройки
                    public void onClick(View v) {
                        /////////////////////////////////////
                        try {
                            if(mode_serv != '3') {                              // если не выбран режим AP
                                name_SSID = login_ssid.getText().toString();
                                pass_SSID = pass_ssid.getText().toString();
                            }else{
                                //name_SSID = "Dimmer_EZ";
                                name_SSID = "Smart_Home_EZ";
                                pass_SSID = "12345678";
//                            mode_serv = '1';
                                getIPfor_client("192.168.4.1");
                            }
                        }catch(Exception e){
                            show_txt_ex();
                        }
                        final Handler handlinfo_startESP = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                info_prog.setText(stat_info_prog);
                            }
                        };
                        /////////////////////////////////////
                        if( name_SSID.length() != 0 && pass_SSID.length() == 8 ) {                      // пароль должен быть 8 символов
                            if(!connect_server || !netSSID_cur.equals("Smart_Home_EZ")){ show_txt_toast("Подождите подключения устройства!"); }
                            else {
                                ////////////////////////////////////
                                if (!thr_start) {                            //чтобы не плодить потоки
                                    initESP_ON = true;                       // при получении ответа о успешном сохранении данных, этот бит нужно сбросить
                                    progrbar.setVisibility(View.VISIBLE);
                                    name_SSID.getChars(0, name_SSID.length(), bufTCPout, 35);
                                    pass_SSID.getChars(0, pass_SSID.length(), bufTCPout, 52);
                                    bufTCPout[51] = (char) name_SSID.length();
                                    bufTCPout[61] = mode_serv;
                                    //                     getIPfor_client();
                                    ////////////////////////////////
                                    flag_dialog = true;
                                    cmd_send = set_link;
                                    count_req++;
                                    Log.d(tag, "set link send command!");
                                    ////////////////////////////////
                                    Log.d(tag, "lenght SSID = " + (int) bufTCPout[51] + " len = " + name_SSID.length());

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            thr_start = true;
                                            int step_progr = 0;
                                            cnt_0_25_sec = 0;
                                            boolean cr_OK = false;
                                            try {
                                                while (initESP_ON) {                                            // в потоке, ожидаем получение подтверждения принятой МК команды set_link, после подтверждения будет сброшен бит initESP_ON

                                                    //Thread.sleep(400);
                                                    if (cnt_0_25_sec % 2 == 0 && !cr_OK) {
                                                        cr_OK = true;
                                                        if (step_progr == 1) {
                                                            stat_info_prog = "Обновление" + "\n" + "параметров.";
                                                        }
                                                        if (step_progr == 2) {
                                                            stat_info_prog = "Обновление" + "\n" + "параметров..";
                                                        }
                                                        if (step_progr == 3) {
                                                            stat_info_prog = "Обновление" + "\n" + "параметров...";
                                                        }
                                                        if (step_progr == 4) {
                                                            stat_info_prog = "Обновление" + "\n" + "параметров....";
                                                        }
                                                        if (step_progr == 5) {
                                                            stat_info_prog = "Обновление" + "\n" + "параметров.....";
                                                        }
                                                        if (step_progr == 6) {
                                                            stat_info_prog = "Обновление" + "\n" + "параметров......";
                                                        }
                                                        step_progr++;
                                                        handlinfo_startESP.sendEmptyMessage(0);
                                                        if (step_progr > 6) {
                                                            step_progr = 0;
                                                        }
                                                        //////////////////////////////////////////////
                                                        //get_name_ssid();
                                                        if (netSSID_cur != null) {
                                                            //if(netSSID_cur.equals("Dimmer_EZ")){ ; }            // проверяем, если подключение к сети Dimmer_EZ.
                                                            if (netSSID_cur.equals("Smart_Home_EZ")) {
                                                                ;
                                                            }            // проверяем, если подключение к сети Smart_home_EZ.
                                                            else {                                              // подключения к сети нет
                                                                //wifi_reconnect_net(cntx, "Dimmer_EZ", "12345678");}
                                                                if (cnt_0_25_sec % 40 == 0)
                                                                    wifi_reconnect_net(cntx, "Smart_Home_EZ", "12345678");  ///раз в 10 сек попытка переподключния
                                                            }
                                                        }
                                                    } else {
                                                        cr_OK = false;
                                                    }
                                                    //////////////////////////////////////////////
                                                }
                                                if (mode_serv != '3') {
                                                    saved_config("server_IP", gotIP(SERVER_IP));
                                                } else {
                                                    saved_config("server_IP", "192.168.4.1");
                                                }
                                            } catch (Exception ee) {
                                                Log.d(tag, "Exception sleep");
                                            }
                                            //////////////////////////////////////////////////////////
                                            thr_start = false;
                                            alert.dismiss();
                                            //////////////////////////////////////////////////////////
                                        }
                                    }).start();

                                }
                            }
                        }
                        else {
                            show_txt_toast("Данные введены не корректно!");
//                            off_wifi(cntx);
                        }
                    }

                };
                apply_conf.setOnClickListener(apply_config_handle);

                break;
            case 2:                                                                     // о приложении
                LinearLayout about_app;
                about_app = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
                final TextView v_head = about_app.findViewById(R.id.Head_v);
                final TextView v_txt = about_app.findViewById(R.id.txt_v);

                v_head.setText("Smart Switch");
                v_txt.setText(R.string.about);

                builder.setView(about_app);
                alert = builder.create();
                alert.show();
                break;
            case 3:
                /*//  debug
                LinearLayout set_bright;
                set_bright = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
                final TextView v_head_br = set_bright.findViewById(R.id.Head_v);
                final TextView v_txt_br = set_bright.findViewById(R.id.txt_v);
                final EditText new_br = new EditText(MainActivity.this);
                final Button newOK = new Button(this);
                final Button newNO = new Button(this);

                LinearLayout new_lay = new LinearLayout(this);
                new_lay.setOrientation(LinearLayout.HORIZONTAL);
                new_lay.addView(newNO); newNO.setText("Отмена");
                new_lay.addView(newOK); newOK.setText("Применить");
                new_br.setHint(Integer.toString(scalegr));
                set_bright.addView(new_br); set_bright.addView(new_lay);
                //new_br.setInputType(InputType.TYPE_CLASS_NUMBER); // цифровая клавиатура, вместо энтер на клаве будет крыжик применить
                LinearLayout.LayoutParams new_lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                new_lp.weight = 1;
                newNO.setLayoutParams(new_lp);
                newOK.setLayoutParams(new_lp);

                v_head_br.setText("Smart Switch");
                v_txt_br.setText("Данные DEBUG");         //

                builder.setView(set_bright);
                alert = builder.create();
                alert.show();
                View.OnClickListener apply_yes = new View.OnClickListener() {           //
                    public void onClick(View v) {
                        if(new_br.getText().toString().equals("")){
                            show_txt_toast( "Вы не ввели данные!" );
                        }else{
                            String str_tmp = new_br.getText().toString();
                            str_tmp.getChars(0, str_tmp.length(), bufTCPout, 68);
                            cmd_send = (int)bufTCPout[68]; wait_ENDSENDTCP = true;
                            Log.d(tag, "Данные DEBUG"+bufTCPout[68]+bufTCPout[69]+bufTCPout[70]+bufTCPout[71]+bufTCPout[72]+bufTCPout[73]+bufTCPout[74]+bufTCPout[75]+bufTCPout[76]+bufTCPout[77]+bufTCPout[78]);
                        }

                        alert.dismiss();
                    }
                };
                newOK.setOnClickListener(apply_yes);
                View.OnClickListener apply_no = new View.OnClickListener() {           //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                newNO.setOnClickListener(apply_no);
                */
                break;
            case 4:                                                                     // вывод справки
                LinearLayout help_app;
                help_app = (LinearLayout) getLayoutInflater().inflate(R.layout.help, null);
                final Button butok = help_app.findViewById(R.id.butOK);
                builder.setView(help_app);
                alert = builder.create();
                alert.show();
                View.OnClickListener ok = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };

                butok.setOnClickListener(ok);
                break;
            case 5:                                                             // установка нового IP, включение IP из списка
                final EditText new_ip = new EditText(MainActivity.this);
                final Button newIPbutOK = new Button(this);
                final Button newIPbutNO = new Button(this);
                ScrollView scroll_v = new ScrollView(this);
                LinearLayout l_scrl = new LinearLayout(this);
                l_scrl.setOrientation(LinearLayout.VERTICAL);
                newIPbutOK.setText("Применить");
                newIPbutNO.setText("Отмена");
                newIPbutOK.setBackgroundColor(getResources().getColor(R.color.new_dark)); newIPbutOK.setTextColor(getResources().getColor(R.color.new_txtcolor));
                newIPbutNO.setBackgroundColor(getResources().getColor(R.color.new_dark)); newIPbutNO.setTextColor(getResources().getColor(R.color.new_txtcolor));
                new_ip.setInputType(InputType.TYPE_CLASS_TEXT); // вместо энтер на клаве будет крыжик применить
                LinearLayout ll=new LinearLayout(this);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                ll.addView(newIPbutNO);
                ll.addView(newIPbutOK);

                LinearLayout.LayoutParams lpp4 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                lpp4.weight = 1;
                lpp4.setMargins(15, 15, 15, 15);
                newIPbutOK.setLayoutParams(lpp4); newIPbutNO.setLayoutParams(lpp4);
//            newIPbutNO.setX(120);       // смещение по X
//            newIPbutOK.setX(250);       // смещение по X
/////////////////////////
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(

                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
//////////////////////////
                final String tmpstr;
                name_adr = new ArrayList<String>();
                num_save_ip = read_config_int("num_save_ip");           // читаем кол-во сохраненных IP
                if(num_save_ip != 0){                                   // заполняем список сохраненными IP
                    for(int dr = 0; dr<num_save_ip; dr++){
                        //name_adr.add(read_config_str("server_IP"+Integer.toString(dr+1))+"    -    "+read_config_str("new_name_obj"+(dr+1)));
                        ////////////////////////////////////////////
                        String sstr = read_config_str("new_name_obj"+(dr+1));
                        if(sstr.equals("")){ sstr = "Объект "+(dr+1); }
                        name_adr.add(read_config_str("server_IP"+Integer.toString(dr+1))+"    -    "+sstr);
                        ////////////////////////////////////////////
                        Log.d(tag, "new_name_obj"+(+dr+1)+" .. "+read_config_str("new_name_obj"+(dr+1)));
                    }

                }

                ////////////////////////////////////////////
                final ListView lst_ipadr = new ListView(MainActivity.this);
//                String[] name_adr = { "Иван", "Марья", "Петр" };
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name_adr);
//                final ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name_adr);
                lst_ipadr.setAdapter(adapter);
                new_IP_found = true;

                LinearLayout lay_forIP;
                lay_forIP = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
                final TextView txtIP = lay_forIP.findViewById(R.id.txt_v);
                final TextView txtHEADER = lay_forIP.findViewById(R.id.Head_v);
                txtHEADER.setText("Smart Switch");
                txtIP.setText("Установка нового IP адреса");
                lay_forIP.addView(new_ip);
//                lay_forIP.addView(lst_ipadr);
                l_scrl.addView(lst_ipadr);
                setListViewHeightBasedOnChildren(lst_ipadr);        // выставляем отбражение всех элементов списка
                l_scrl.addView(ll);
                scroll_v.addView(l_scrl);
                lay_forIP.addView(scroll_v);

                tmpstr = SERVER_IP;
                new_ip.setHint(SERVER_IP);

                builder.setView(lay_forIP);
                alert = builder.create();
                alert.show();

                View.OnClickListener OKIP = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        close_TCP();
                        SERVER_IP = new_ip.getText().toString();
                        if(SERVER_IP.equals(""))SERVER_IP = tmpstr;
                        else{
                            /////////////////////////////////////////////////////
                            if(SERVER_IP.equals(read_config_str("server_IP"))){ new_IP_found = false;}          // если в памяти у же есть этот IP, то сбрасываем флаг new_IP_found - сохранять не будем
                            for(int ip = 0; ip<num_save_ip ; ip++){
                                if(SERVER_IP.equals(read_config_str("server_IP"+Integer.toString(ip+1)))){ new_IP_found = false;}        //// если в памяти у же есть этот IP, то сбрасываем флаг new_IP_found - сохранять не будем
                            }
                            /////////////////////////////////////////////////////          SERVER_IP != tmpstr
                            if(new_IP_found) {
                                num_save_ip++;
                                saved_config("num_save_ip", num_save_ip);
                                select_pos = num_save_ip; saved_config("select_pos", select_pos);           // текущая позиция из списка
                                saved_config("server_IP"+Integer.toString(num_save_ip), SERVER_IP);
                            }
                        }
                        saved_config("server_IP", SERVER_IP);                   // сохраняем IP для следующего старта приложения
                        name_serv.setText("IP server "+SERVER_IP);                 // " port "+ port_int
                        room_name.setText("Объект "+num_save_ip);
                        alert.dismiss();
                    }
                };
                newIPbutOK.setOnClickListener(OKIP);
                View.OnClickListener NOIP = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                newIPbutNO.setOnClickListener(NOIP);
                ///////////////////////////////////////
                lst_ipadr.setOnItemClickListener(new AdapterView.OnItemClickListener() {    // еще один вариант обработчика нажатия на пункт списка
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Log.d(tag, "itemClick: position = " + position + ", id = "
                                + id+ " всего адресов в списке "+ " ::: "+num_save_ip);
                        close_TCP();
//                        SERVER_IP =  name_adr.get(position);
                        SERVER_IP = read_config_str("server_IP"+(position+1));
                        select_pos = position+1; saved_config("select_pos", select_pos);           // текущая позиция из списка
                        saved_config("server_IP", SERVER_IP);                           // сохраняем IP для следующего старта приложения
                        name_serv.setText("IP server "+SERVER_IP);                         //" port "+ port_int
                        if(read_config_str("new_name_obj"+select_pos).equals("")){ room_name.setText("Объект "+select_pos);}
                        else{ room_name.setText(read_config_str("new_name_obj"+select_pos)); }
                        load_data_strONOF();        /// загрузка сохраненноых для этого IP записей
                        alert.dismiss();
                    }
                });
                ////////////////////////
                lst_ipadr.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {    // еще один вариант обработчика нажатия на пункт списка
                    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                                   int position, long id) {

                        select_pos = position;
                        dialog2_show(1);
                        alert.dismiss();
                        return true;
                    }

                });
                ////////////////////////
                break;
            case 6:                                                             // перименование подключенного объекта, включение объекта из списка
                ScrollView scroll_vn = new ScrollView(this);
                LinearLayout l_scrln = new LinearLayout(this);
                l_scrln.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams lpp3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                lpp3.rightMargin = 15;
                lpp3.leftMargin = 15;
                lpp3.bottomMargin = 15;

                final Button butNO = new Button(this);
                butNO.setText("Отмена");
                butNO.setBackgroundColor(getResources().getColor(R.color.new_dark)); butNO.setTextColor(getResources().getColor(R.color.new_txtcolor));
                butNO.setLayoutParams(lpp3);
                List<String> name_adrn;

                name_adr = new ArrayList<String>();
                name_adrn = new ArrayList<String>();
                String tmp_str = "";
                num_save_ip = read_config_int("num_save_ip");           // читаем кол-во сохраненных IP
                if(num_save_ip != 0){                                   // заполняем список сохраненными IP
                    for(int dr = 0; dr<num_save_ip; dr++){
                        name_adr.add(read_config_str("server_IP"+Integer.toString(dr+1)));
                        tmp_str = read_config_str("new_name_obj"+Integer.toString(dr+1));
                        if(tmp_str.equals(""))tmp_str = "Объект "+(dr+1);
                        name_adrn.add(tmp_str);
                    }
                }

                final ListView lst_ipadrn = new ListView(MainActivity.this);
                final ArrayAdapter<String> adaptern = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name_adrn);
                lst_ipadrn.setAdapter(adaptern);

                LinearLayout lay_forIPn;
                lay_forIPn = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
                final TextView txtIPn = lay_forIPn.findViewById(R.id.txt_v);
                final TextView txtHEADERn = lay_forIPn.findViewById(R.id.Head_v);
                txtHEADERn.setText("Smart Switch");
                txtIPn.setText("Выбор объекта");

                l_scrln.addView(lst_ipadrn);
                setListViewHeightBasedOnChildren(lst_ipadrn);        // выставляем отбражение всех элементов списка
                scroll_vn.addView(l_scrln);
                lay_forIPn.addView(scroll_vn);
                l_scrln.addView(butNO);

                builder.setView(lay_forIPn);
                alert = builder.create();
                alert.show();

                View.OnClickListener chanel = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                butNO.setOnClickListener(chanel);
                lst_ipadrn.setOnItemClickListener(new AdapterView.OnItemClickListener() {    // еще один вариант обработчика нажатия на пункт списка
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Log.d(tag, "itemClick: position = " + position + ", id = "
                                + id+ " всего адресов в списке "+ " ::: "+num_save_ip);
                        close_TCP();
                        SERVER_IP =  name_adr.get(position);
                        select_pos = position+1; saved_config("select_pos", select_pos);           // текущая позиция из списка
                        saved_config("server_IP", SERVER_IP);                           // сохраняем IP для следующего старта приложения
                        name_serv.setText("IP server "+SERVER_IP);                         //" port "+ port_int
                        if(read_config_str("new_name_obj"+select_pos).equals("")){ room_name.setText("Объект "+select_pos);}
                        else{ room_name.setText(read_config_str("new_name_obj"+select_pos)); }
                        load_data_strONOF();        /// загрузка сохраненноых для этого IP записей
                        alert.dismiss();
                    }
                });
                ////////////////////////
                lst_ipadrn.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {    // еще один вариант обработчика нажатия на пункт списка
                    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                                   int position, long id) {
                        new_select_pos = position+1;
//                        select_pos = position+1;
                        Log.d(tag, " pos_до переименования "+select_pos+", pos_после переименования "+ new_select_pos);
                        dialog2_show(2);
                        alert.dismiss();
                        return true;
                    }

                });
                break;
            case 7:
                LinearLayout viewST;
                viewST = (LinearLayout) getLayoutInflater().inflate(R.layout.lay_set_tmp, null);
                final TextView st_txt = viewST.findViewById(R.id.fonlay_txt);
                final TextView edit_st = viewST.findViewById(R.id.settmp_txt);
                final LinearLayout lay111 = viewST.findViewById(R.id.lay_for_txtedit);
                final TextView new_yes = viewST.findViewById(R.id.text_ye);
                final TextView new_NO = viewST.findViewById(R.id.text_no);
                st_txt.setText("Для перехода в меню настроек кликните пиктограмму \"домика\" в верхнем правом углу экрана.\n\nПоказывать это напоминание при старте?");
                lay111.removeView(edit_st);

                builder.setView(viewST);
                alert = builder.create();
                alert.show();
                View.OnClickListener handl_newyes = new View.OnClickListener() {        // да
                    public void onClick(View v) {
                        saved_config("saved_show_help", 0);
                        alert.dismiss();
                    }
                };
                new_yes.setOnClickListener(handl_newyes);
                View.OnClickListener handl_newNO = new View.OnClickListener() {          // нет
                    public void onClick(View v) {
                        saved_config("saved_show_help", 1);
                        alert.dismiss();
                    }

                };
                new_NO.setOnClickListener(handl_newNO);
                break;
            case 8:                                                                         /// Режим "Сутки"
                set_DAYweek = "Режим 'Сутки'"; num_dayweek = 8;
                sel_MODE = 1;           /// выбран режим работы сутки
                dialog2_show(3);
                break;
            case 9:                                                                         ///// Режим "Неделя"
                LinearLayout lay_1 = new LinearLayout(this);
                ///lay_1.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams lpp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                lpp.rightMargin = 80; lpp1.rightMargin = 80;                //
                lpp.leftMargin = 80;  lpp1.leftMargin = 80;                //
                lpp1.bottomMargin = 60; lpp1.topMargin = 15;
                lpp.bottomMargin = 2;

                final Button but_day1 = new Button(this); but_day1.setBackgroundColor(getResources().getColor(R.color.new_dark)); but_day1.setTextColor(getResources().getColor(R.color.new_txtcolor));
                final Button but_day2 = new Button(this); but_day2.setBackgroundColor(getResources().getColor(R.color.new_dark)); but_day2.setTextColor(getResources().getColor(R.color.new_txtcolor));
                final Button but_day3 = new Button(this); but_day3.setBackgroundColor(getResources().getColor(R.color.new_dark)); but_day3.setTextColor(getResources().getColor(R.color.new_txtcolor));
                final Button but_day4 = new Button(this); but_day4.setBackgroundColor(getResources().getColor(R.color.new_dark)); but_day4.setTextColor(getResources().getColor(R.color.new_txtcolor));
                final Button but_day5 = new Button(this); but_day5.setBackgroundColor(getResources().getColor(R.color.new_dark)); but_day5.setTextColor(getResources().getColor(R.color.new_txtcolor));
                final Button but_day6 = new Button(this); but_day6.setBackgroundColor(getResources().getColor(R.color.new_dark)); but_day6.setTextColor(getResources().getColor(R.color.new_txtcolor));
                final Button but_day7 = new Button(this); but_day7.setBackgroundColor(getResources().getColor(R.color.new_dark)); but_day7.setTextColor(getResources().getColor(R.color.new_txtcolor));
                final Button but_dayOK = new Button(this); but_dayOK.setBackgroundColor(getResources().getColor(R.color.new_dark)); but_dayOK.setTextColor(getResources().getColor(R.color.new_txtcolor));
                but_day1.setText("Понедельник");
                but_day2.setText("Вторник");
                but_day3.setText("Среда");
                but_day4.setText("Четверг");
                but_day5.setText("Пятница");
                but_day6.setText("Суббота");
                but_day7.setText("Воскресенье");
                but_dayOK.setText("OK");
                but_day1.setLayoutParams(lpp); but_day2.setLayoutParams(lpp); but_day3.setLayoutParams(lpp);
                but_day4.setLayoutParams(lpp); but_day5.setLayoutParams(lpp); but_day6.setLayoutParams(lpp);
                but_day7.setLayoutParams(lpp); but_dayOK.setLayoutParams(lpp1);

                lay_1 = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
                final TextView txt_mes = lay_1.findViewById(R.id.txt_v);
                final TextView txtHEADER_lay1 = lay_1.findViewById(R.id.Head_v);
                txtHEADER_lay1.setText("Режим 'Неделя'");
                txt_mes.setText("Выберите день недели");

                lay_1.addView(but_day1); lay_1.addView(but_day2); lay_1.addView(but_day3); lay_1.addView(but_day4);
                lay_1.addView(but_day5); lay_1.addView(but_day6); lay_1.addView(but_day7); lay_1.addView(but_dayOK);
                but_day1.setId(R.id.ID_but1week); but_day2.setId(R.id.ID_but2week); but_day3.setId(R.id.ID_but3week);
                but_day4.setId(R.id.ID_but4week); but_day5.setId(R.id.ID_but5week); but_day6.setId(R.id.ID_but6week);
                but_day7.setId(R.id.ID_but7week); but_dayOK.setId(R.id.ID_butweekOK);

                builder.setView(lay_1);
                alert = builder.create();
                alert.show();
                //////////////
                View.OnClickListener butweek_handl = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        sel_MODE = 3;           /// выбран режим работы неделя
                        switch(v.getId()){
                            case R.id.ID_but1week:
                               // dialog2_show(3);
                                set_DAYweek = "Понедельник"; num_dayweek = 2;
                                dialog2_show(3);
                                break;
                            case R.id.ID_but2week:
                                set_DAYweek = "Вторник"; num_dayweek = 3;
                                dialog2_show(3);
                                break;
                            case R.id.ID_but3week:
                                set_DAYweek = "Среда"; num_dayweek = 4;
                                dialog2_show(3);
                                break;
                            case R.id.ID_but4week:
                                set_DAYweek = "Четверг"; num_dayweek = 5;
                                dialog2_show(3);
                                break;
                            case R.id.ID_but5week:
                                set_DAYweek = "Пятница"; num_dayweek = 6;
                                dialog2_show(3);
                                break;
                            case R.id.ID_but6week:
                                set_DAYweek = "Суббота"; num_dayweek = 7;
                                dialog2_show(3);
                                break;
                            case R.id.ID_but7week:
                                set_DAYweek = "Воскресенье"; num_dayweek = 1;
                                dialog2_show(3);
                                break;
                            case R.id.ID_butweekOK:
                                    alert.dismiss();
                                break;
                        }

                     }
                };
                but_day1.setOnClickListener(butweek_handl); but_day2.setOnClickListener(butweek_handl); but_day3.setOnClickListener(butweek_handl);
                but_day4.setOnClickListener(butweek_handl); but_day5.setOnClickListener(butweek_handl); but_day6.setOnClickListener(butweek_handl);
                but_day7.setOnClickListener(butweek_handl); but_dayOK.setOnClickListener(butweek_handl);
                /////////////
                break;
            case 10:                                                                            //// Режим "Вкл. по дате"
                set_DAYweek = "Режим 'Вкл. по дате'"; //num_dayweek = 9;
                sel_MODE = 2;           /// выбран режим работы сутки
                dialog2_show(3);
                break;
            case 11:                                                                            //// Синхронизация расписания с модулем
                //dialog2_show(4);
                LinearLayout wait_synchroLay;
                wait_synchroLay = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
                final TextView w_head = wait_synchroLay.findViewById(R.id.Head_v);
                final TextView w_txt = wait_synchroLay.findViewById(R.id.txt_v);
                final ProgressBar prg_bar = new ProgressBar(this);
                wait_synchroLay.addView(prg_bar);
                prg_bar.setVisibility(View.VISIBLE); // View.GONE

                w_head.setText("Smart Switch");
                w_txt.setText("Синхронизация установок..");

                builder.setView(wait_synchroLay);
                alert = builder.create();
                alert.show();
                synchro_OK = true;
                if(connect_server) {        /// если есть соединение с сервером
                    String str = "value______day.lua";
                    //String str = "value_schedule.lua";
                    //String str = "valueweek_day5.lua";
                    str.getChars(0, 18, bufTCPout, 69);
                    flag_dialog = true;
                    cmd_send = get_schedule;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            thr_start = true;
                            try {
                                while (synchro_OK) {                                            // в потоке, ожидаем получение подтверждения принятой МК команды set_link, после подтверждения будет сброшен бит initESP_ON

                                }
                            } catch (Exception ee) {
                                Log.d(tag, "Exception sleep");
                            }
                            //////////////////////////////////////////////////////////
                            thr_start = false;
                            alert.dismiss();
                            //////////////////////////////////////////////////////////
                        }
                    }).start();
                }else{ show_txt_toast("Нет соединения с сервером!"); alert.dismiss(); }
                break;
            case 12:                                                                            //// Синхронизация времени между андроид и модулем
                timeMillis = System.currentTimeMillis();        /// получение системного времени в милисекундах прошедшего после 01.01.1970(для отправки модули нужно перейти к секундам, поделив результат на 1000)
                timeMillis /=1000;
                String str_val =  ""+timeMillis;
                flag_dialog = true;
                int len_str = str_val.length();
                if(len_str <18){
                    for(int i = 0; i<(18-len_str); i++){ str_val = '0'+str_val; }
                }

                TimeZone tz = TimeZone.getDefault();        /// получить часовой пояс
                String str_shift =  ""+ (tz.getRawOffset()/1000);    /// tz.getRawOffset() - получаем смещение в миллисек, /1000 - в секундах
                Log.d(tag, "TimeZone "+str_shift);
                bufTCPout[65] = (char)str_shift.length();

                Log.d(tag, "timeMillis "+str_val+" str_shift "+str_shift);
                str_val.getChars(0, str_val.length(), bufTCPout, 69);       //// в виде строки отправляем данные для синхро(как в названии должно быть 18 символов)
                str_shift.getChars(0, str_shift.length(), bufTCPout, 87);       //// в виде строки отправляем смещение для текущего часового пояса
                cmd_send = set_time_module;
                break;
            case 13:                                                        //// сброс модуля к установкам по умолчанию
                LinearLayout reset_mod;
                reset_mod = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
                final TextView res_head = reset_mod.findViewById(R.id.Head_v);
                final TextView res_txt = reset_mod.findViewById(R.id.txt_v);
                final Button rstbutOK = new Button(this);
                final Button rstbutNO = new Button(this);
                LinearLayout lay_3 = new LinearLayout(this);
                lay_3.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams lpp_param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                lpp_param.topMargin = 15;
                lpp_param.bottomMargin = 15;
                lpp_param.leftMargin = 20;
                lpp_param.rightMargin = 20;
                lpp_param.weight = 1;
                rstbutOK.setLayoutParams(lpp_param);
                rstbutNO.setLayoutParams(lpp_param);

                rstbutOK.setText("Да");
                rstbutNO.setText("Отмена");
                rstbutOK.setBackgroundColor(getResources().getColor(R.color.new_dark)); rstbutOK.setTextColor(getResources().getColor(R.color.new_txtcolor));
                rstbutNO.setBackgroundColor(getResources().getColor(R.color.new_dark)); rstbutNO.setTextColor(getResources().getColor(R.color.new_txtcolor));
                lay_3.addView(rstbutOK);
                lay_3.addView(rstbutNO);
                reset_mod.addView(lay_3);

                res_head.setText("Smart Switch");
                res_txt.setTextColor(getResources().getColor(R.color.RED));
                res_txt.setText(R.string.rst_mod);

                builder.setView(reset_mod);
                alert = builder.create();
                alert.show();

                View.OnClickListener NO_rst = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                rstbutNO.setOnClickListener(NO_rst);
                View.OnClickListener YES_rst = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        flag_dialog = true;
                        cmd_send = set_link;
                        alert.dismiss();
                    }
                };
                rstbutOK.setOnClickListener(YES_rst);
                break;
        }
    }
    /////////////////////////////////////////////////resiev_IP
    void dialog2_show(int dialog) {
//        flag_dialog = true;
        final AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Log.d(tag, "Start_dialog 2");
        switch (dialog) {
            case 1:                                                                         /// удаление выбранного IP адреса
                final Button newIPbutOK = new Button(this);
                final Button newIPbutNO = new Button(this);
                newIPbutOK.setText("Да");
                newIPbutNO.setText("Отмена");
                newIPbutOK.setBackgroundColor(getResources().getColor(R.color.new_dark)); newIPbutOK.setTextColor(getResources().getColor(R.color.new_txtcolor));
                newIPbutNO.setBackgroundColor(getResources().getColor(R.color.new_dark)); newIPbutNO.setTextColor(getResources().getColor(R.color.new_txtcolor));
                LinearLayout lay_forIP;
                LinearLayout lay_for_button = new LinearLayout(this);
                lay_for_button.setOrientation(LinearLayout.HORIZONTAL);
                lay_forIP = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
                final TextView txtIP = lay_forIP.findViewById(R.id.txt_v);
                final TextView txtHEADER = lay_forIP.findViewById(R.id.Head_v);
                txtHEADER.setText("Smart Switch");
                String nameobj = read_config_str("new_name_obj"+Integer.toString(select_pos+1));
                if(nameobj.equals(""))nameobj = Integer.toString(select_pos+1);
                txtIP.setText("Удалить объект "+nameobj+"?");
//Log.d(tag, "del - "+read_config_str("new_name_obj"+Integer.toString(select_pos+1))+ "  select_pos = "+select_pos);
                LinearLayout.LayoutParams lpp5 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                lpp5.weight = 1;                 // установить вес 1
                lpp5.setMargins(15, 15, 15, 15);
                newIPbutOK.setLayoutParams(lpp5); newIPbutNO.setLayoutParams(lpp5);
                lay_for_button.addView(newIPbutOK); lay_for_button.addView(newIPbutNO);
                lay_forIP.addView(lay_for_button);

                builder.setView(lay_forIP);
                alert = builder.create();
                alert.show();

                View.OnClickListener OKIP = new View.OnClickListener() {        //
                    public void onClick(View v) {
//////////////////
                        int newposID = 1;
//                        if(select_pos>0) {
                        for (int ip = 0; ip < select_pos; ip++) {                 // перезаписываем IP адреса
//                                saved_config("server_IP" + newposID, name_adr.get(ip));
                            newposID++;
                            Log.d(tag, name_adr.get(ip) + "  " + newposID);
                        }
                        for (int ip = select_pos + 1; ip < num_save_ip; ip++) {                 // перезаписываем IP алреса
                            //  saved_config("server_IP" + newposID, name_adr.get(ip));
                            saved_config("server_IP" + newposID, read_config_str("server_IP"+Integer.toString(ip+1)));
                            saved_config("new_name_obj"+newposID, read_config_str("new_name_obj"+Integer.toString(ip+1)));
                            newposID++;
                            Log.d(tag, name_adr.get(ip) + "  " + newposID);
                        }
                        saved_config("new_name_obj"+newposID, "");
                        num_save_ip--;
                        saved_config("num_save_ip", num_save_ip);               // сохраняем новое количество IP
                        Log.d(tag, "Удаление пункта списка "+select_pos);
                        alert.dismiss();
                    }
                };
                newIPbutOK.setOnClickListener(OKIP);
                View.OnClickListener NOIP = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                newIPbutNO.setOnClickListener(NOIP);
                break;
            case 2:                                                                     // переименование объекта
                final EditText new_name = new EditText(MainActivity.this);
                final Button newbutOK = new Button(this);
                final Button newbutNO = new Button(this);

                newbutOK.setText("Применить");
                newbutNO.setText("Отмена");
                new_name.setInputType(InputType.TYPE_CLASS_TEXT); // вместо энтер на клаве будет крыжик применить
                LinearLayout lll=new LinearLayout(this);
                lll.setOrientation(LinearLayout.HORIZONTAL);
                lll.addView(newbutNO);
                lll.addView(newbutOK);

                LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                lpp.weight = 1;                 // установить вес 1
                lpp.setMargins(15, 15, 15, 15);
                newbutNO.setLayoutParams(lpp);
                newbutOK.setLayoutParams(lpp);
                newbutNO.setBackgroundColor(getResources().getColor(R.color.new_dark)); newbutNO.setTextColor(getResources().getColor(R.color.new_txtcolor));
                newbutOK.setBackgroundColor(getResources().getColor(R.color.new_dark)); newbutOK.setTextColor(getResources().getColor(R.color.new_txtcolor));

                LinearLayout lay_forname;
                lay_forname = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
                final TextView txt_n = lay_forname.findViewById(R.id.txt_v);
                final TextView txtH_n = lay_forname.findViewById(R.id.Head_v);
                txtH_n.setText("Smart Switch");
                txt_n.setText("Новое название объекта");
                new_name.setHint("Гостинная");
                lay_forname.addView(new_name);
                lay_forname.addView(lll);
                builder.setView(lay_forname);
                alert = builder.create();
                alert.show();
                ////////////////////////
                View.OnClickListener YEScreate = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        String str_new_obj;
                        str_new_obj = new_name.getText().toString();
                        saved_config("new_name_obj"+ new_select_pos, str_new_obj);
//                        room_name.setText(str_new_obj);
                        str_new_obj = read_config_str("new_name_obj"+select_pos);
                        if(str_new_obj.equals(""))str_new_obj = "Объект "+select_pos;
                        room_name.setText(str_new_obj);

                        alert.dismiss();
                    }
                };
                newbutOK.setOnClickListener(YEScreate);
                View.OnClickListener NOcreate = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                newbutNO.setOnClickListener(NOcreate);
                break;
            case 3:                                                                 // добавление записи вкл/выкл любого из режимов
                LinearLayout.LayoutParams lpp_param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams param_laybut = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                lpp_param.topMargin = 30;
                lpp_param.leftMargin = 20;
                lpp_param.rightMargin = 20;

                param_laybut.rightMargin = 20;
                param_laybut.leftMargin = 20;
                param_laybut.bottomMargin = 5;
                param_laybut.weight = 1;

                ScrollView scroll_lay = new ScrollView(this);
                Button btnback_lst = new Button(this);
                Button btndel_lst = new Button(this);
                Button btnadd_item = new Button(this);
                LinearLayout lay_1 = new LinearLayout(this);
                LinearLayout lay_2 = new LinearLayout(this);
                LinearLayout lay_3 = new LinearLayout(this);
                lay_2.setOrientation(LinearLayout.VERTICAL);
                lay_3.setOrientation(LinearLayout.HORIZONTAL);
                lay_1 = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
                final TextView txt_mes = lay_1.findViewById(R.id.txt_v);
                final TextView txtHEADER_lay1 = lay_1.findViewById(R.id.Head_v);

                btnback_lst.setText("Отмена");
                btnadd_item.setText("Добавить");
                btndel_lst.setText("Очистить список");
                btnback_lst.setBackgroundColor(getResources().getColor(R.color.new_dark)); btnback_lst.setTextColor(getResources().getColor(R.color.new_txtcolor));
                btnadd_item.setBackgroundColor(getResources().getColor(R.color.new_dark)); btnadd_item.setTextColor(getResources().getColor(R.color.new_txtcolor));
                btndel_lst.setBackgroundColor(getResources().getColor(R.color.new_dark)); btndel_lst.setTextColor(getResources().getColor(R.color.new_txtcolor));

                txtHEADER_lay1.setText(set_DAYweek);
                txt_mes.setText("Настройки времени вкл/выкл");
                lvSimple = new ListView(MainActivity.this);
                bar_waitCMD = lay_1.findViewById(R.id.progressBar_waitCMD);
                lay_2.addView(lvSimple);
                lay_3.addView(btnadd_item);
                lay_3.addView(btnback_lst);
                lay_2.addView(lay_3);
                lay_2.addView(btndel_lst);
                scroll_lay.addView(lay_2);
                lay_1.addView(scroll_lay);

                lvSimple.setLayoutParams(lpp_param);
                btnadd_item.setLayoutParams(param_laybut);
                btnback_lst.setLayoutParams(param_laybut);
                btndel_lst.setLayoutParams(param_laybut);
                ///////////////////////

                // имена атрибутов для Map
                final String ATTRIBUTE_NAME_TEXT = "text";
                final String ATTRIBUTE_NAME_TEXT1 = "text1";
                //               final String ATTRIBUTE_NAME_CHECKED = "checked";
                final String ATTRIBUTE_NAME_IMAGE = "image";
                /////////////////////////
                int var_selmode = sel_MODE;
                if(sel_MODE == 3){ var_selmode += num_dayweek; }
                data_str = new ArrayList<Map<String, Object>>( num_str_setONOF[var_selmode] ); //// объявляем массив строк размерностью num_str_setONOF[var_selmode]

                update_list(var_selmode, num_str_setONOF[var_selmode]);
                ///////////////////////////
                // массив имен атрибутов, из которых будут читаться данные
/*                String[] from = { ATTRIBUTE_NAME_TEXT, ATTRIBUTE_NAME_CHECKED,
                        ATTRIBUTE_NAME_IMAGE };*/
                String[] from = { ATTRIBUTE_NAME_TEXT1, ATTRIBUTE_NAME_TEXT,
                        ATTRIBUTE_NAME_IMAGE };
                // массив ID View-компонентов, в которые будут вставлять данные
//                int[] to = { R.id.tvText, R.id.cbChecked, R.id.ivImg };
                int[] to = { R.id.tvText1, R.id.tvText, R.id.ivImg };

                // создаем адаптер
                sAdapter = new SimpleAdapter(this, data_str, R.layout.item,
                        from, to);
                // определяем список и присваиваем ему адаптер
                //lvSimple = (ListView) findViewById(R.id.lvSimple);
                lvSimple.setAdapter(sAdapter);
                 //////////////////////////
                ViewGroup.LayoutParams params = lvSimple.getLayoutParams();
                params.height = calculateHeight(lvSimple);              //// находим высоту списка
                lvSimple.setLayoutParams(params);                       //// устанавливаем нужную высоту
                // lvSimple.requestLayout();
//////////////////////////
                //////////////////////////////////////

                builder.setView(lay_1);
                alert = builder.create();
                alert.show();

                View.OnClickListener handladd = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        // alert.dismiss();
                        if(sel_MODE == 2){ dialog_Datesel(); }  //// если выбран режим по дате, то вызываем календарь
                        else{ dialog_Timesel(); }               //// выбираем время для записи вкл/выкл
                    }
                };
                btnadd_item.setOnClickListener(handladd);
                View.OnClickListener handldellAll = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        if(sel_MODE == 3) {
                            if (num_str_setONOF[(sel_MODE + num_dayweek)] > 0) dialog_DELETE_req(1);
                        }else {
                            if (num_str_setONOF[sel_MODE] > 0) dialog_DELETE_req(1);
                        }
                        /////////////////////////////
                        // alert.dismiss();
                    }
                };
                btndel_lst.setOnClickListener(handldellAll);    ///// очистить все записи
                View.OnClickListener handlBack = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                btnback_lst.setOnClickListener(handlBack);
                ///////////////////////
                lvSimple.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {    // еще один вариант обработчика нажатия на пункт списка
                    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                                   int position, long id) {
//                        show_txt_toast("Выполнено!");
                        int cnt_dimpos;
                        if(sel_MODE == 3) { cnt_dimpos = sel_MODE + num_dayweek; }
                        else { cnt_dimpos = sel_MODE; }
                        if (num_str_setONOF[cnt_dimpos] > 0) {
                            str_dataForDel = str_setONOF[cnt_dimpos][position];
                            edit_listDT(position, cnt_dimpos);      // подготавливаем массив с удаленной строкой, в слуае подтверждения удаления переписываем его в массив с данными
                            dialog_DELETE_req(2);
                        }
                        //alert.dismiss();
                        return true;
                    }

                });
                ///////////////////////
                break;
            case 4:

            break;
        }
    }
    ///////////////////////////////////
    void edit_listDT(int pos, int cnt_par){    ///  редактирование списка записей, временно сохраняем отредактированный список в массиве str_setONOF_tmp[], записывается массив с исключенным пуктом pos
        for(int i = 0, k = 0; i<num_str_setONOF[cnt_par]; i++){
            if(i != pos){ str_setONOF_tmp[k] = str_setONOF[cnt_par][i]; k++; }
        }
//        for(int i = 0; i<num_str_setONOF[cnt_par]; i++){ Log.d(tag, "out list["+cnt_par+"]"+"["+i+"]"+str_setONOF_tmp[i]+" pos "+pos); }
    }
    ///////////////////////////////////
    void update_list(int num_str, int cnt){   ///// num_str - номер первого эл-та в двумерном массиве, отвечает за режим(сутки, дата, день недели). cnt - кол- во записей в этом режиме.
        int img;
        final String ATTRIBUTE_NAME_TEXT = "text";
        final String ATTRIBUTE_NAME_TEXT1 = "text1";
//        Log.d(tag, "update_list num_str "+num_str+" cnt "+cnt);
        //               final String ATTRIBUTE_NAME_CHECKED = "checked";
        final String ATTRIBUTE_NAME_IMAGE = "image";
        for (int i = 0; i < cnt; i++) {
            Obj_m = new HashMap<String, Object>();
            Obj_m.put(ATTRIBUTE_NAME_TEXT, str_setONOF[num_str][i].substring(1));   /// выводим строку начиная с 1 символа, 0 - символ индикация вкл/выкл
//                    m.put(ATTRIBUTE_NAME_CHECKED, checked[i]);
            if(str_setONOF[num_str][i].charAt(0)== '1'){
                img = R.drawable.on_ico;
                Obj_m.put(ATTRIBUTE_NAME_TEXT1, "вкл. ");
            }
            else {
                img = R.drawable.off_ico;
                Obj_m.put(ATTRIBUTE_NAME_TEXT1, "выкл. ");
            }
            Obj_m.put(ATTRIBUTE_NAME_IMAGE, img);
            data_str.add(Obj_m);
        }

    }
    ///////////////////
    final Handler info_bad_status_cmd = new Handler() {
        @Override
        public void handleMessage(Message msg) {                //// обработчик, не получили ответ на отправленную команду
            show_txt_toast("Команда не выполнена, нет ответа от модуля!");
            bar_waitCMD.setVisibility(View.GONE);
        }
    };
    ///////////////////
    ///////////////////////////////////
    void dialog_DELETE_req(int dial){       /// диалог удаления записей и списков вкл/выкл
        final AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(dial) {
            case 1:                         //// удаление списка параметров вкл/выкл
                LinearLayout.LayoutParams lpp_param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                lpp_param.rightMargin = 20;
                lpp_param.leftMargin = 20;
                lpp_param.bottomMargin = 30;
                lpp_param.topMargin = 50;
                lpp_param.weight = 1;
                LinearLayout lay_1 = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
                LinearLayout lay_2 = new LinearLayout(this);
                lay_2.setOrientation(LinearLayout.HORIZONTAL);
                final TextView txt_mes = lay_1.findViewById(R.id.txt_v);
                final TextView txtHEADER_lay1 = lay_1.findViewById(R.id.Head_v);
                Button btnselON = new Button(this);
                Button btnselOFF = new Button(this);
                btnselOFF.setLayoutParams(lpp_param);
                btnselON.setLayoutParams(lpp_param);
                btnselOFF.setBackgroundColor(getResources().getColor(R.color.new_dark)); btnselOFF.setTextColor(getResources().getColor(R.color.new_txtcolor));
                btnselON.setBackgroundColor(getResources().getColor(R.color.new_dark)); btnselON.setTextColor(getResources().getColor(R.color.new_txtcolor));
                txtHEADER_lay1.setText("Smart Switch");
                txt_mes.setText("Внимание!\n Список настроек времени включения - выключения будет удален безвозвратно, вы уверены что хотите удалить его?");
                btnselON.setText("Удалить");
                btnselOFF.setText("Отмена");
                lay_2.addView(btnselOFF);
                lay_2.addView(btnselON);
                lay_1.addView(lay_2);
                builder.setView(lay_1);
                alert = builder.create();
                alert.show();

                final Handler del_updataList_adapt = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {                       //// обработчик, команда удаления списка прошла успешно, список удален
                        int num_last = num_str_setONOF[sel_MODE_var];
                        num_str_setONOF[sel_MODE_var] = 0;
                        data_str.clear();
                        update_list(sel_MODE_var, num_str_setONOF[sel_MODE_var]);
                        sAdapter.notifyDataSetChanged();
                        ViewGroup.LayoutParams params = lvSimple.getLayoutParams();
                        params.height = calculateHeight(lvSimple);              //// находим высоту списка
                        lvSimple.setLayoutParams(params);                       //// устанавливаем нужную высоту
                        bar_waitCMD.setVisibility(View.GONE);
                        for(int i = 0; i<num_last; i++) str_setONOF_tmp[i] = "";
                        saved_config(SERVER_IP+sel_MODE_var, str_setONOF_tmp, num_str_setONOF[sel_MODE_var]);
                    }
                };
                View.OnClickListener handl_btnselON = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        /////////////////////////////
                        flag_dialog = true;
                        sel_MODE_var = sel_MODE;
                        if (sel_MODE == 3) {
                            sel_MODE_var += num_dayweek;
                        }
                        switch (sel_MODE) {
                            case 1:
                                cmd_send = del_ALLSETday;
                                break;
                            case 2:
                                cmd_send = del_ALLSET_data;
                                break;
                            case 3:
                                bufTCPout[64] = (char) num_dayweek;
                                cmd_send = del_ALLSET_week;
                                break;

                        }
                        new Thread(new Runnable() {                     ///  в потоке вызываем обработчики принятой/не принятой команды подтверждения
                            @Override
                            public void run() {

                                cnt_answ = 0;
                                Log.d(tag, "cmd_OK "+cmd_OK);
                                while(!cmd_OK){ if(cnt_answ>20)break;  }    /// ждем ответа 10сек максимально
                                if(cmd_OK) {
                                    del_updataList_adapt.sendEmptyMessage(0);              // команда прошла успешно
                                    cmd_OK = false;
                                }else{
                                    info_bad_status_cmd.sendEmptyMessage(0);        // не получили подтверждения отправленой команды
                                }

                               // del_updataList_adapt.sendEmptyMessage(0);
                            }}).start();
                        ///////////////
                        ///////////
                        alert.dismiss();
                    }
                };
                btnselON.setOnClickListener(handl_btnselON);
                View.OnClickListener handl_btnselOFF = new View.OnClickListener() {        //
                    public void onClick(View v) {

                        alert.dismiss();
                    }
                };
                btnselOFF.setOnClickListener(handl_btnselOFF);
                break;
            case 2:                                            //// удаление выбраной записи параметров вкл/выкл
                LinearLayout.LayoutParams lpp_param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                lpp_param1.rightMargin = 20;
                lpp_param1.leftMargin = 20;
                lpp_param1.bottomMargin = 30;
                lpp_param1.topMargin = 50;
                lpp_param1.weight = 1;
                LinearLayout lay_11 = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
//                bar_waitCMD = lay_11.findViewById(R.id.progressBar_waitCMD);
                LinearLayout lay_21 = new LinearLayout(this);
                lay_21.setOrientation(LinearLayout.HORIZONTAL);
                final TextView txt_mes1 = lay_11.findViewById(R.id.txt_v);
                final TextView txtHEADER_lay11 = lay_11.findViewById(R.id.Head_v);
                Button btnselON1 = new Button(this);
                Button btnselOFF1 = new Button(this);
                btnselOFF1.setLayoutParams(lpp_param1);
                btnselON1.setLayoutParams(lpp_param1);
                btnselON1.setBackgroundColor(getResources().getColor(R.color.new_dark)); btnselON1.setTextColor(getResources().getColor(R.color.new_txtcolor));
                btnselOFF1.setBackgroundColor(getResources().getColor(R.color.new_dark)); btnselOFF1.setTextColor(getResources().getColor(R.color.new_txtcolor));
                txtHEADER_lay11.setText("Smart Switch");
                txt_mes1.setText("Вы уверены что хотите удалить запись?");
                btnselON1.setText("Удалить");
                btnselOFF1.setText("Отмена");
                lay_21.addView(btnselOFF1);
                lay_21.addView(btnselON1);
                lay_11.addView(lay_21);
                builder.setView(lay_11);
                alert = builder.create();
                alert.show();
                final Handler del_updata_adapt = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {                       //// обработчик, команда удаления записи прошла успешно, обновляем список записей вкл/выкл
                        num_str_setONOF[sel_MODE_var]--;
                        for(int k = 0; k<num_str_setONOF[sel_MODE_var]; k++){ str_setONOF[sel_MODE_var][k] = str_setONOF_tmp[k]; } /// переписываем массив с отредактированным временным списком в нужный нам массив str_setONOF[var_selmode1][k]
                        ////////////////////////////////////преобразуем запись из формата для вывода на экран в формат записи в устройстве и заполняем буфер для отправки серверу
                        revers_data_for_SCREEN(str_dataForDel);
                        ////////////////////////////////////
                        data_str.clear();
                        update_list(sel_MODE_var, num_str_setONOF[sel_MODE_var]);
                        sAdapter.notifyDataSetChanged();
                        ViewGroup.LayoutParams params = lvSimple.getLayoutParams();
                        params.height = calculateHeight(lvSimple);              //// находим высоту списка
                        lvSimple.setLayoutParams(params);                       //// устанавливаем нужную высоту
                        bar_waitCMD.setVisibility(View.GONE);
                        for(int i = 0; i<num_str_setONOF[sel_MODE_var]; i++) str_setONOF_tmp[i] = str_setONOF[sel_MODE_var][i];
                        saved_config(SERVER_IP+sel_MODE_var, str_setONOF_tmp, num_str_setONOF[sel_MODE_var]);
                    }
                };
                View.OnClickListener handl_btnselON1 = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        /////////////////////////////
                        flag_dialog = true;
                        sel_MODE_var = sel_MODE;
                        bar_waitCMD.setVisibility(View.VISIBLE);
                        if (sel_MODE == 3) {
                            sel_MODE_var += num_dayweek;
                        }
                        switch (sel_MODE) {
                            case 1:
                                cmd_send = delSET_day;
                                break;
                            case 2:
                                cmd_send = delSET_data;
                                break;
                            case 3:
                                bufTCPout[64] = (char) num_dayweek;
                                cmd_send = delSET_week;
                                break;

                        }
                    /*
                        num_str_setONOF[var_selmode1]--;
                        for(int k = 0; k<num_str_setONOF[var_selmode1]; k++){ str_setONOF[var_selmode1][k] = str_setONOF_tmp[k]; } /// переписываем массив с отредактированным временным списком в нужный нам массив str_setONOF[var_selmode1][k]
                        ////////////////////////////////////преобразуем запись из формата для вывода на экран в формат записи в устройстве и заполняем буфер для отправки серверу
                        revers_data_for_SCREEN(str_dataForDel);
                        ////////////////////////////////////
                        data_str.clear();
                        update_list(var_selmode1, num_str_setONOF[var_selmode1]);
                        sAdapter.notifyDataSetChanged();
                        ViewGroup.LayoutParams params = lvSimple.getLayoutParams();
                        params.height = calculateHeight(lvSimple);              //// находим высоту списка
                        lvSimple.setLayoutParams(params);                       //// устанавливаем нужную высоту
*/
                    ///////////////
                        new Thread(new Runnable() {                     ///  в потоке вызываем обработчики принятой/не принятой команды подтверждения
                            @Override
                            public void run() {
                                cnt_answ = 0;
                                Log.d(tag, "cmd_OK "+cmd_OK);
                                while(!cmd_OK){ if(cnt_answ>20)break;  }    /// ждем ответа 10сек максимально
                                if(cmd_OK) {
                                    del_updata_adapt.sendEmptyMessage(0);              // команда прошла успешно
                                    cmd_OK = false;
                                }else{
                                    info_bad_status_cmd.sendEmptyMessage(0);        // не получили подтверждения отправленой команды
                                }
                            }}).start();
                    ///////////////
                        alert.dismiss();
                    }
                };
                btnselON1.setOnClickListener(handl_btnselON1);
                View.OnClickListener handl_btnselOFF1 = new View.OnClickListener() {        //
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                };
                btnselOFF1.setOnClickListener(handl_btnselOFF1);
                break;
        }
    }
    ///////////////////////////////////
    String revers_data_for_SCREEN(String strdata){   /// данные в массиве для отображения на экране приводит к формату для передачи модулю и сразу копирует в массив bufTCPout

    String dt_str = "";
    char[] symb_str = new char[strdata.length()];
  //      Log.d(tag, "revers_data_for_SCREEN "+strdata);
        strdata.getChars(0, strdata.length(), symb_str, 0);
        dt_str = dt_str+symb_str[0]+":";

        if(sel_MODE == 2){          /// если выбран режим работы по дате
            if(symb_str[1] != '0'){ for(int i = 0; i<3; i++) dt_str = dt_str+symb_str[i+1]; }
            else{ for(int i = 0; i<2; i++) dt_str = dt_str+symb_str[i+2]; }
            if(symb_str[4] != '0'){ for(int i = 0; i<3; i++) dt_str = dt_str+symb_str[i+4]; }
            else{ for(int i = 0; i<2; i++) dt_str = dt_str+symb_str[i+5];}
            for(int i = 0; i<4; i++){ dt_str = dt_str+symb_str[i+7];}
            dt_str = dt_str+".";
            if(symb_str[13] != '0'){ for(int i = 0; i<2; i++){ dt_str = dt_str+symb_str[i+13];} }
            else{ dt_str = dt_str+symb_str[14]; }
            dt_str = dt_str+".";
            if(symb_str[16] != '0'){ for(int i = 0; i<2; i++){ dt_str = dt_str+symb_str[i+16];} }
            else{ dt_str = dt_str+symb_str[17]; }
            dt_str = dt_str+"."+"0";
        }else{
            if(symb_str[1] != '0'){ for(int i = 0; i<2; i++) dt_str = dt_str+symb_str[i+1]; }
            else { dt_str = dt_str+symb_str[2]; }
            dt_str = dt_str+".";
            if(symb_str[4] != '0'){ for(int i = 0; i<2; i++) dt_str = dt_str+symb_str[i+4]; }
            else { dt_str = dt_str+symb_str[5]; }
            dt_str = dt_str+"."+"0";
        }
 //       Log.d(tag, "revers_data_for_SCREEN_return "+dt_str);
        bufTCPout[64] = (char)num_dayweek;
        bufTCPout[65] = (char)dt_str.length();            /// количество символов для передачи
        dt_str.getChars(0, dt_str.length(), bufTCPout, 87); // копирование символов строки в массив bufTCPout
        return dt_str;
    }

    /////////////////////////////////// Диалог выбора времени
    void dialog_Timesel() {
//        flag_dialog = true;
        final AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
            TimePickerDialog tpd = new TimePickerDialog(this, myCallBack, myHourD, myMinuteD, true);
            alert = tpd;
            alert.show();

    }
    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHourD = hourOfDay;
            myMinuteD = minute;
            dialog_selONOF();                                                       /// вызов диалога добавления записи
            Log.d(tag, "Time is " + myHourD + " hours " + myMinuteD + " minutes");
        }
    };
    /////////////////////////////////// Диалог выбора даты
    public void dialog_Datesel() {
        dateAndTime=Calendar.getInstance();
        new DatePickerDialog(MainActivity.this, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            selYear = year;
            selMon = monthOfYear;
            selDay = dayOfMonth;
            dialog_Timesel();
            Log.d(tag, "Date is " + selDay + "." + selMon + "."+selYear);
        }
    };
    ///////////////////////////////////
    void dialog_selONOF() {           //// диалог добавления записи вкл/выкл
//        flag_dialog = true;
        final AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout.LayoutParams lpp_param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lpp_param.rightMargin = 20;
        lpp_param.leftMargin = 20;
        lpp_param.bottomMargin = 30;
        lpp_param.topMargin = 50;
        lpp_param.weight = 1;
        LinearLayout lay_1 = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
        LinearLayout lay_2 = new LinearLayout(this);
        lay_2.setOrientation(LinearLayout.HORIZONTAL);
        lay_1 = (LinearLayout) getLayoutInflater().inflate(R.layout.fon_munu, null);
        final TextView txt_mes = lay_1.findViewById(R.id.txt_v);
        final TextView txtHEADER_lay1 = lay_1.findViewById(R.id.Head_v);

        Button btnselON = new Button(this);
        Button btnselOFF = new Button(this);
        btnselOFF.setLayoutParams(lpp_param);
        btnselON.setLayoutParams(lpp_param);
        btnselON.setText("Время вкл.");
        btnselOFF.setText("Время выкл.");
        btnselON.setBackgroundColor(getResources().getColor(R.color.new_dark)); btnselON.setTextColor(getResources().getColor(R.color.new_txtcolor));
        btnselOFF.setBackgroundColor(getResources().getColor(R.color.new_dark)); btnselOFF.setTextColor(getResources().getColor(R.color.new_txtcolor));

        if(myMinuteD < 10){ txtHEADER_lay1.setText("Выбрано время "+myHourD+":0"+myMinuteD); }
        else{ txtHEADER_lay1.setText("Выбрано время "+myHourD+":"+myMinuteD); }

        txt_mes.setText("Применить как ");
        lay_2.addView(btnselON); lay_2.addView(btnselOFF);
        lay_1.addView(lay_2);

        builder.setView(lay_1);
        alert = builder.create();
        alert.show();
        final Handler updata_adapt = new Handler() {
            @Override
            public void handleMessage(Message msg) {                       //// обработчик, команда добавления записи прошла успешно, обновляем список записей вкл/выкл
                num_str_setONOF[sel_MODE_var]++;
                str_setONOF[sel_MODE_var][(num_str_setONOF[sel_MODE_var] - 1)] = data_for_SCREEN(sel_MODE, str_data);   /// преобразуем запись к формату для вывода на экран и сохраняем в таком виде в массиве  str_setONOF[][]              //"012.12.2020  07:00";
                data_str.clear();
                update_list(sel_MODE_var, num_str_setONOF[sel_MODE_var]);
                sAdapter.notifyDataSetChanged();
//                Log.d(tag, "высоту всего списка 3 " + " list.getCount " + lvSimple.getCount());
                ViewGroup.LayoutParams params = lvSimple.getLayoutParams();
                params.height = calculateHeight(lvSimple);              //// находим высоту списка
                lvSimple.setLayoutParams(params);                       //// устанавливаем нужную высоту
                bar_waitCMD.setVisibility(View.GONE);
                for(int i = 0; i<num_str_setONOF[sel_MODE_var]; i++) str_setONOF_tmp[i] = str_setONOF[sel_MODE_var][i];
                saved_config(SERVER_IP+sel_MODE_var, str_setONOF_tmp, num_str_setONOF[sel_MODE_var]);
            }
        };
        View.OnClickListener handl_btnselOFF = new View.OnClickListener() {        //
            public void onClick(View v) {
                bar_waitCMD.setVisibility(View.VISIBLE);
                sel_MODE_var = sel_MODE;
                flag_dialog = true;
                switch(sel_MODE){
                    case 1:                 /// режим сутки
                        str_data = ready_strdata_for_writeESP(0, myHourD, myMinuteD);       /// подготовка записи в нужном формате
                        bufTCPout[65] = (char)str_data.length();        /// количество символов для передачи
                        cmd_send = addSET_day;
                        break;
                    case 2:                 /// режим дата
                        str_data = ready_strdata_for_writeESP(0, "Date", myHourD, myMinuteD);       /// подготовка записи в нужном формате
                        bufTCPout[65] = (char)str_data.length();        /// количество символов для передачи
                        cmd_send = addSET_data;
                        break;
                    case 3:                 /// режим неделя
                        sel_MODE_var += num_dayweek;
                        str_data = ready_strdata_for_writeESP(0, num_dayweek, myHourD, myMinuteD);  /// подготовка записи в нужном формате
                        bufTCPout[64] = (char)num_dayweek;
                        bufTCPout[65] = (char)str_data.length();        /// количество символов для передачи
                        cmd_send = addSET_week;
                        break;
                }
                ///////////
                Log.d(tag, "режим "+str_data);
                str_data.getChars(0, str_data.length(), bufTCPout, 87); // копирование символов строки в массив bufTCPout
//                Log.d(tag, "cmd_OK__ startOFF");
                ///////////
                new Thread(new Runnable() {                     ///  в потоке вызываем обработчики принятой/не принятой команды подтверждения
                    @Override
                    public void run() {

                        cnt_answ = 0;
                        int cnt_tmp = cnt_answ;
                        while(!cmd_OK){
                            if(cnt_answ>40)break;               /// ждем ответа 10сек максимально
                            if(cnt_tmp != cnt_answ) { Log.d(tag, "cmd_OK__ "+cmd_OK+" cnt_answ "+cnt_answ); cnt_tmp = cnt_answ; };
                        }
                        if(cmd_OK) {
                            updata_adapt.sendEmptyMessage(0);              // команда прошла успешно
                            cmd_OK = false; cnt_answ = 0;
                        }else{
                            info_bad_status_cmd.sendEmptyMessage(0);        // не получили подтверждения отправленой команды
                        }


                        //updata_adapt.sendEmptyMessage(0);
                    }}).start();
 //               Log.d(tag, "cmd_OK__ start_threadOFF");
                alert.dismiss();
            }
        };
        btnselOFF.setOnClickListener(handl_btnselOFF);
        View.OnClickListener handl_btnselON = new View.OnClickListener() {        //
            public void onClick(View v) {
                bar_waitCMD.setVisibility(View.VISIBLE);
                sel_MODE_var = sel_MODE;
                flag_dialog = true;
                switch(sel_MODE){
                    case 1:                 /// режим сутки
                        str_data = ready_strdata_for_writeESP(1, myHourD, myMinuteD);
                        bufTCPout[65] = (char)str_data.length();        /// количество символов для передачи
                        cmd_send = addSET_day;
                        break;
                    case 2:                 /// режим дата
                        str_data = ready_strdata_for_writeESP(1, "Date", myHourD, myMinuteD);
                        bufTCPout[65] = (char)str_data.length();        /// количество символов для передачи
                        cmd_send = addSET_data;
                        break;
                    case 3:                 /// режим неделя
                        sel_MODE_var += num_dayweek;
                        str_data = ready_strdata_for_writeESP(1, num_dayweek, myHourD, myMinuteD);
                        bufTCPout[64] = (char)num_dayweek;
                        bufTCPout[65] = (char)str_data.length(); /// количество символов для передачи
                        cmd_send = addSET_week;
                        break;
                }
                Log.d(tag, "режим "+str_data);
                str_data.getChars(0, str_data.length(), bufTCPout, 87); // копирование символов строки в массив bufTCPout
                ///////////
//                Log.d(tag, "cmd_OK__ startON");
                new Thread(new Runnable() {                     ///  в потоке вызываем обработчики принятой/не принятой команды подтверждения
                    @Override
                    public void run() {
                        cnt_answ = 0;
                        int cnt_tmp = cnt_answ;
                        Log.d(tag, "cmd_OK "+cmd_OK);
                        while(!cmd_OK){
                            if(cnt_answ>40)break;                   /// ждем ответа 10сек максимально
                            if(cnt_tmp != cnt_answ) { Log.d(tag, "cmd_OK__ "+cmd_OK+" cnt_answ "+cnt_answ); cnt_tmp = cnt_answ; };
                        }
                        if(cmd_OK) {
                            updata_adapt.sendEmptyMessage(0);              // команда прошла успешно
                            cmd_OK = false;
                        }else{
                            info_bad_status_cmd.sendEmptyMessage(0);        // не получили подтверждения отправленой команды
                        }

                    }}).start();
//                Log.d(tag, "cmd_OK__ start_threadON");
                alert.dismiss();
            }
        };
        btnselON.setOnClickListener(handl_btnselON);
    }
    ///////////////// Конвертирование данных записей из формата для модуля в формат для вывода на экран
    String data_for_SCREEN(int mode, String str){
       String dat = ""+str.charAt(0);                       //// копируем нулевой символ(0 или 1, вкл/выкл)
       String str_tmp = "";
       int cnt;
            if(mode != 2){                                  //// для строки суточного и недельного режима
                for(cnt = 2; str.charAt(cnt)!= '.'; cnt++){ str_tmp = str_tmp+str.charAt(cnt); }
                if( Integer.parseInt(str_tmp)<10){ dat  = dat+"0"+str_tmp+":"; }
                else dat  = dat+str_tmp+":";
                str_tmp = ""; cnt++;
                for(int i = cnt; str.charAt(cnt)!= '.'; i++, cnt++){ str_tmp = str_tmp+str.charAt(i); }
                if( Integer.parseInt(str_tmp)<10){ dat  = dat+"0"+str_tmp; }
                else dat  = dat+str_tmp;
            }else{                                          //// для сторки режима по дате
                for(cnt = 2; str.charAt(cnt)!= '.'; cnt++){ str_tmp = str_tmp+str.charAt(cnt); }
                if( Integer.parseInt(str_tmp)<10){ dat  = dat+"0"+str_tmp+"."; }
                else dat  = dat+str_tmp+".";
                str_tmp = ""; cnt++;
                for(int i = cnt; str.charAt(cnt)!= '.'; i++, cnt++){ str_tmp = str_tmp+str.charAt(i); }
                if( Integer.parseInt(str_tmp)<10){ dat  = dat+"0"+str_tmp+"."; }
                else dat  = dat+str_tmp+".";
                str_tmp = ""; cnt++;
                for(int i = cnt; str.charAt(cnt)!= '.'; i++, cnt++){ str_tmp = str_tmp+str.charAt(i); }
                dat  = dat+str_tmp;
                str_tmp = ""; cnt++;
                dat  = dat +"  ";
                for(int i = cnt; str.charAt(cnt)!= '.'; i++, cnt++){ str_tmp = str_tmp+str.charAt(i); }
                if( Integer.parseInt(str_tmp)<10){ dat  = dat+"0"+str_tmp+":"; }
                else dat  = dat+str_tmp+":";
                str_tmp = ""; cnt++;
                for(int i = cnt; str.charAt(cnt)!= '.'; i++, cnt++){ str_tmp = str_tmp+str.charAt(i); }
                if( Integer.parseInt(str_tmp)<10){ dat  = dat+"0"+str_tmp; }
                else dat  = dat+str_tmp;
            }
//        Log.d(tag, "режим  2 dat "+dat+" str_tmp "+str_tmp+" cnt "+cnt);
       return dat;
    }
    ///////////////////////////////////////////////////////////////////////
    String ready_strdata_for_writeESP(int ONOFF, int day, int hour, int minute){   /// для режима работы неделя
        String value = "";
 //       value = value+ day+"'"+ONOFF+":"+hour+"."+minute;
        value = value+ ONOFF+":"+hour+"."+minute+".0";
        return value;
    }
    String ready_strdata_for_writeESP(int ONOFF, int hour, int minute){            /// для режима работы сутки
        String value = "";
        value = ONOFF+":"+hour+"."+minute+".0";
        return value;
    }
    String ready_strdata_for_writeESP(int ONOFF, String date, int hour, int minute){   /// для режима работы дата
        String value = "";
        String dt = "";
        dt = dt+selDay + "." + (selMon+1) + "."+selYear+ ".";
//        Log.d(tag, "cur_data_cl "+dt);
        ///////////////////
        value = value+ ONOFF+":"+dt+hour+"."+minute+".0";
        return value;
    }
    /////////////////////
    private int calculateHeight(ListView list) {  //// определяет высоту всего списка
        int height = 0;
        int cnt_liststr = list.getCount();
 //       if(cnt_liststr > 6) { cnt_liststr = 6; }
            for (int i = 0; i < cnt_liststr; i++) {
                View childView = list.getAdapter().getView(i, null, list);
                childView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                height += childView.getMeasuredHeight();
            }
        /* // определяет высоту одного элемента списка
        View childView = list.getAdapter().getView(0, null, list);
        childView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        height = childView.getMeasuredHeight();
        */

            height += list.getDividerHeight() * cnt_liststr;
            list.setSelection(list.getCount()-1);           //// установка фокуса списка на последнюю позицию
//   list.setVerticalScrollBarEnabled(true);
////            Log.d(tag, "высоту всего списка " + height + " list.getCount " + list.getCount());
        return height;
    }
        ////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////
    void wifi_reconnect_net(Context context, String ssid, String key){

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + ssid + "\"";
//        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        if(key.equals("open")){;}
///        else { conf.preSharedKey = String.format("\"%s\"", key);}
        else { conf.preSharedKey = "\"" + key + "\"";}
        Log.d(tag, "conf. "+conf.SSID + "  "+conf.preSharedKey);
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        /* вариант подключения к нужной сети, но почему-то нет подкл. к серверу
        wifiManager.addNetwork(conf);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                break;
            }
        }
        */
        int netId = wifiManager.addNetwork(conf);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

    }
    ////////////////////////////////////////
    boolean getIPfor_client(String str){ // возвращает true при корректном IP и заполняет массив bufTCPout символами адреса
        //////////////////////////////
        boolean ok = false;
        char [] ipserv = new char[str.length()+1];
        char [] tmp_ip = new char[4];
        try {
            str.getChars(0, str.length(), ipserv, 0);
            ipserv[str.length()] = '.';
            for (int i = 0, t = 0, j = 0; i < str.length()+1; i++) {
                tmp_ip[t] = ipserv[i];
                if (ipserv[i] != '.') {
                    t++;
                } else {
                    switch (t) {
                        case 1:
                            bufTCPout[19 + j] = '0';
                            bufTCPout[19 + 1 + j] = '0';
                            bufTCPout[19 + 2 + j] = tmp_ip[0];
                            break;
                        case 2:
                            bufTCPout[19 + j] = '0';
                            bufTCPout[19 + 1 + j] = tmp_ip[0];
                            bufTCPout[19 + 2 + j] = tmp_ip[1];
                            break;
                        case 3:
                            bufTCPout[19 + j] = tmp_ip[0];
                            bufTCPout[19 + 1 + j] = tmp_ip[1];
                            bufTCPout[19 + 2 + j] = tmp_ip[2];
                            break;
                    }
                    t = 0;
                    j += 3;
                }
            }
            Log.d(tag, "ip_serv " + bufTCPout[19] + bufTCPout[20] + bufTCPout[21] + bufTCPout[22] + bufTCPout[23] + bufTCPout[24] + bufTCPout[25] + bufTCPout[26] + bufTCPout[27] + bufTCPout[28] + bufTCPout[29] + bufTCPout[30]);
            ok = true;
            //////////////////////////////
        }catch(Exception t){ Log.d(tag, "Exception getIPfor_client");  ok = false; }
        return ok;
    }
    /////////////////////////////////////////////////
    String gotIP(String str){ // преобразует IP формата 192.168.004.001 к формату 192.168.4.1
        //////////////////////////////
        String strIP = "";
        int j = 0;
        char [] ipserv = new char[str.length()+1];
        char [] tmp_ip = new char[4];
        try {
            str.getChars(0, str.length(), ipserv, 0);
            ipserv[str.length()] = '.';
            for (int i = 0, t = 0; i < str.length()+1; i++) {
                tmp_ip[t] = ipserv[i]; t++;
                if (ipserv[i] == '.') {
                    if(tmp_ip[0]=='0'){ j = 1; }
                    if(tmp_ip[1]=='0' && j == 1) { j = 2; }
                    if(tmp_ip[2]=='0' && j == 2) { j = 3; }
                    switch(j){
                        case 0:
                            strIP = strIP +tmp_ip[0]+tmp_ip[1]+ tmp_ip[2];
                            break;
                        case 1:
                            strIP = strIP + tmp_ip[1]+ tmp_ip[2];
                            break;
                        case 2:
                            strIP = strIP +tmp_ip[2];
                            break;
                        case 3:
                            strIP = strIP+'0';
                            break;
                    }
                    if(i !=15)strIP =strIP + '.';
                    //               strIP =strIP + tmp_ip[0]+tmp_ip[1]+ tmp_ip[2];
                    t = 0; j =0;
                }
            }
//        Log.d(tag, " strIP "+strIP);

            //////////////////////////////
        }catch(Exception t){ Log.d(tag, "Exception gotIPfor");  }
        return strIP;
    }
    /////////////////////////////////////////////////
    void show_txt_ex(){
        Toast.makeText(this,"Введены не все данные!", Toast.LENGTH_SHORT).show();
    }
    /////////////////////////////////////////////////
    // в параметрах передаём listView для определения высоты
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
         int totalHeight = 0;
        // проходимся по элементам коллекции
         for(int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                // получаем высоту
                totalHeight += listItem.getMeasuredHeight();
         }
        // устанавливаем новую высоту для контейнера
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
    ///////////
    String cur_data(){
        Date curTime = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy  HH:mm");  // задаем формат даты
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");  // задаем формат даты
        String sdt_= sdf.format(curTime);
//        txt_dev.setText(sdt_);
        return sdt_;
    }
    /////////////
    void delayMs(int time){
        try {

            Thread.sleep(time);
        } catch (Exception ee) {
            Log.d(tag, "Exception sleep");
        }
    }
    //////////////////////////////////////////////
    class TimerTask2 extends TimerTask {

        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    "dd:MMMM:yyyy HH:mm:ss a", Locale.getDefault());
            final String strDate = simpleDateFormat.format(calendar.getTime());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                mCounterTextView.setText(strDate);
                    dialog_show(7);
                }
            });
        }
    }
    //////////////////////////////////////////////
    @Override
    protected void onStop() {
        super.onStop();
      //  unregisterReceiver(wifi_BroadcastReceiver);
    }
}
