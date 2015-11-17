package com.example.uf.floatingactionsmenudemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import floating.FloatingActionMenu;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionMenu floatingActionMenu = (FloatingActionMenu) findViewById(R.id.floating_actions_menu);
       //设置菜单展开方向（不设置则按xml中设置为准）
        floatingActionMenu.setmExpandDirection(FloatingActionMenu.EXPAND_UP);
        ImageButton imageButton1 = new ImageButton(this);
        imageButton1.setBackgroundResource(R.drawable.icon_share_qq);
        ImageButton imageButton2 = new ImageButton(this);
        imageButton2.setBackgroundResource(R.drawable.icon_share_sinaweibo);

        //添加一个布局菜单
        final View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_childview, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"123",Toast.LENGTH_SHORT).show();
            }
        });
        floatingActionMenu.addActionsView(view);
        floatingActionMenu.addActionsView(imageButton1);
        floatingActionMenu.addActionsView(imageButton2);

    }
}
