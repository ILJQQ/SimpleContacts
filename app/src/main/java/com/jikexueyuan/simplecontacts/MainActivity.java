package com.jikexueyuan.simplecontacts;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Button btnAddContacts;
    private static final String[] PHONES_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
//    联系人显示名称
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;

//    电话号码
    private static final int PHONES_NUMBER_INDEX = 1;
    EditText etName, etPhone;
//    申明listview对象
    ListView lvContacts;
//    建立arraylist用于存储通讯录信息
    ArrayList<HashMap<String, String>> contactsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        申明联系人类型
        final Contact people = new Contact();
//        绑定事件
        lvContacts = (ListView) findViewById(R.id.lvContacts);

//        调用通讯录获取方法
        getPhoneContacts();

//        为每一个联系人添加点击监听事件
        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String items[] = {"打电话", "发短信"};

                new AlertDialog.Builder(MainActivity.this)
                        .setItems(items, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                通过switch判断用户选择
                                switch (items[which]) {
//                                    但选择为打电话时跳转到系统拨号界面并输入对应电话号
                                    case "打电话":
                                        Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri
                                                .parse("tel:" + contactsList.get(position).get("Phone")));
                                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                            // TODO: Consider calling
                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.
                                            return;
                                        }
                                        startActivity(dialIntent);
                                        break;
//                                    但选择为发短信时跳转到系统短信界面并在对话框输入感谢用户使用
                                    case "发短信":
                                        String body = "感谢使用SimpleContacts";
                                        Intent msgIntent = new Intent(Intent.ACTION_SENDTO , Uri
                                                .parse("smsto:" + contactsList.get(position).get("Phone")))
                                                .putExtra("sms_body",body);
                                        startActivity(msgIntent);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).show();
            }
        });

        btnAddContacts = (Button) findViewById(R.id.btnAddContacts);
        btnAddContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.dialog, (ViewGroup) findViewById(R.id.dialog));

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("添加联系人：")
                        .setView(layout)
                        .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "您取消了向通讯录中添加联系人", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                etName = (EditText) layout.findViewById(R.id.etName);
                                etPhone = (EditText) layout.findViewById(R.id.etPhone);
                                people.setName(etName.getText().toString());
                                people.setPhone(etPhone.getText().toString());
                                if (etName.length() == 0 || etPhone.length() == 0) {
                                    Toast.makeText(MainActivity.this, "请输入姓名及电话号码", Toast.LENGTH_SHORT).show();
                                } else {
                                    toContacts(people.getName(), people.getPhone());
                                    Toast.makeText(MainActivity.this, "联系人" + people.getName() + ": 添加成功", Toast.LENGTH_SHORT).show();
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("Name", etName.getText().toString());
                                    map.put("Phone", etPhone.getText().toString());
                                    contactsList.add(map);
                                    SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this,contactsList,R.layout.contact_list,
                                        new String[] {"Name", "Phone"},
                                        new int[] {R.id.list_text,R.id.sub_text });
                                    lvContacts.setAdapter(simpleAdapter);
                                }
                            }
                        }).show()
                ;
            }
        });
    }

//    调用contentresolver获取到系统通讯录内容并添加到listview中
    private void getPhoneContacts() {
        ContentResolver resolver = getContentResolver();
        // 获取手机联系人
        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,PHONES_PROJECTION, null, null, null);

        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {

                //得到手机号码
                String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                //当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber))
                    continue;

                //得到联系人名称
                String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                HashMap<String, String> map = new HashMap<>();
                map.put("Name", contactName);
                map.put("Phone", phoneNumber);
                contactsList.add(map);
            }
            SimpleAdapter simpleAdapter = new SimpleAdapter(this,contactsList,R.layout.contact_list,
                    new String[] {"Name", "Phone"},
                    new int[] {R.id.list_text,R.id.sub_text });
            lvContacts.setAdapter(simpleAdapter);
            phoneCursor.close();
        }
    }


//    添加输入联系人到系统通讯录 传入参数
    private void toContacts(String name, String phone) {
        ContentValues values = new ContentValues();
        Uri rawContactUri = getContentResolver().insert(
                ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
//        添加姓名
        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
//        添加电话号码
        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
    }
}
