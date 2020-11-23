package com.example.foret.Activity.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.foret.R;
import com.example.foret.model.Foret;
import com.example.foret.model.ForetBoard;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView mBottomNV;
    DrawerLayout drawer;
    long pressedTime = 0;
    Toast toast;

    AsyncHttpClient client;
    Fragment fragment;

    // mainFragment foret
    List<Foret> foretList;
    ForetResponse foretResponse;
    String url_foret = "http://192.168.0.2:8081/project/foret/foret_list.do";

    // mainFragment foretBoard
    List<ForetBoard> foretBoardList;
    ForetBoardResponse foretBoardResponse;
    String url_foretBoard = "http://192.168.0.2:8081/project/foret/foret_board_list.do";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Log.d("[TEST]", "1");
        foretList = new ArrayList<>();
        client = new AsyncHttpClient();
        foretResponse = new ForetResponse();
        client.post(url_foret, foretResponse);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawer = findViewById(R.id.mainLayout);
        NavigationView navigationView = findViewById(R.id.nav_view2);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void BottomNavigate(int id) {  // BottomNavigation 페이지 변경
        String tag = String.valueOf(id);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment currentFragment = fragmentManager.getPrimaryNavigationFragment();
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment);
        }

        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {

            if (id == R.id.navigation_1) { // 메인 화면
                fragment = new MainFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("foretList", (Serializable) foretList);
                bundle.putSerializable("foretBoardList", (Serializable) foretBoardList);
                fragment.setArguments(bundle);
                Log.d("[TEST]", "메인 프래그");

            } else if (id == R.id.navigation_2) {   // 익명 게시판 화면
                fragment = new BoardFragment();
            } else if  (id == R.id.navigation_3) {  // 추천/검색 화면
                fragment = new SearchFragment();
            } else if  (id == R.id.navigation_4) {  // 채팅 화면
                fragment = new ChatFragment();
            } else if  (id == R.id.navigation_5) {  // 알림 화면
                fragment = new NoticeFragment();
            }

            fragmentTransaction.add(R.id.content_layout, fragment, tag);
        } else {
            fragmentTransaction.show(fragment);
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragment);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commitNow();


    }

    @Override
    public void onBackPressed() {
        drawer = findViewById(R.id.mainLayout);
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
            return;
        }
        if(pressedTime == 0) {
            toast = Toast.makeText(MainActivity.this, "한 번 더 누르면 종료됩니다.",
                    Toast.LENGTH_SHORT);
            toast.show();
            pressedTime = System.currentTimeMillis();
        } else {
            int seconds = (int)(System.currentTimeMillis() - pressedTime);
            if(seconds > 2000) {
                toast = Toast.makeText(MainActivity.this, "한 번 더 누르면 종료됩니다.",
                        Toast.LENGTH_SHORT);
                toast.show();
                pressedTime = 0;
            } else {
                super.onBackPressed();
                toast.cancel();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the side_nav_icon_menu;
        // 수행 표시줄에 항목이 있는 경우 이 항목이 추가됨.
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 여기서 작업 표시줄 항목 클릭 처리
        // AndroidManifest.xml에서 상위 활동을 지정하는 경우 작업 표시줄에서 Home/Up 버튼 클릭을 자동으로 처리
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.nav :
                drawer = findViewById(R.id.mainLayout);
                drawer.openDrawer(GravityCompat.END);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // 여기에서 탐색 보기 항목 클릭 처리
        int id = item.getItemId();

        DrawerLayout drawer = findViewById(R.id.mainLayout);
        drawer.closeDrawer(GravityCompat.END);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    ///////////////////////////////////////////////////////////////
    class ForetResponse extends AsyncHttpResponseHandler {

        @Override
        public void onStart() {
            Log.d("[TEST]", "Foret 통신 시작");
        }

        @Override
        public void onFinish() {
            Log.d("[TEST]", "Foret 통신 종료");
            foretBoardList = new ArrayList<>();
            foretBoardResponse = new ForetBoardResponse();
            RequestParams param = new RequestParams();
            param.put("group_no", 1);
            client.post(url_foretBoard, param, foretBoardResponse);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                String rt = json.getString("rt");
                int total = json.getInt("total");
                JSONArray item = json.getJSONArray("item");

                for(int i=0; i<item.length(); i++) {
                    JSONObject temp = item.getJSONObject(i);
                    Foret foret = new Foret();
                    foret.setGroup_no(temp.getInt("group_no"));
                    foret.setGroup_name(temp.getString("group_name"));
                    foret.setGroup_currunt_member_count(temp.getInt("group_currunt_member_count"));
                    foret.setGroup_leader(temp.getString("group_leader"));
                    foret.setGroup_profile(temp.getString("group_profile"));
                    foret.setGroup_photo(temp.getString("group_photo"));
                    foret.setGroup_tag(temp.getString("group_tag"));
                    foret.setGroup_region(temp.getString("group_region"));
                    foret.setGroup_max_member(temp.getInt("group_max_member"));
                    foret.setGroup_date_issued(temp.getString("group_date_issued"));

                    String photo_name = temp.getString("photo_name");
                    if (photo_name.equals("")) photo_name = "0";
                    foret.setPhoto_name(photo_name);

                    // list에 저장
                    foretList.add(foret);
                    Log.d("[TEST]", "foretList 통신 성공 => " + foretList.size());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(MainActivity.this, "Foret 통신 실패", Toast.LENGTH_SHORT).show();
        }
    }

    //////////////////////////////////////////////////////////////////////
    class ForetBoardResponse extends AsyncHttpResponseHandler {
        @Override
        public void onStart() {
            Log.d("[TEST]", "ForetBoard 통신 시작");
        }
        @Override
        public void onFinish() {
            Log.d("[TEST]", "ForetBoard 통신 종료");
            Log.d("[TEST]", "bottomNV");
            mBottomNV = findViewById(R.id.nav_view);
            mBottomNV.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() { //NavigationItemSelecte
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    BottomNavigate(menuItem.getItemId());
                    return true;
                }
            });
            mBottomNV.setSelectedItemId(R.id.navigation_1);
        }
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                String rt = json.getString("rt");
                int total = json.getInt("total");
                JSONArray item = json.getJSONArray("item");

                for(int i=0; i<item.length(); i++) {
                    JSONObject temp = item.getJSONObject(i);
                    ForetBoard foretBoard = new ForetBoard();
                    foretBoard.setGroup_no(temp.getInt("group_no"));
                    foretBoard.setBoard_no(temp.getInt("board_no"));
                    foretBoard.setBoard_type(temp.getInt("board_type"));
                    foretBoard.setBoard_writer(temp.getString("board_writer"));
                    foretBoard.setBoard_subject(temp.getString("board_subject"));
                    foretBoard.setBoard_content(temp.getString("board_content"));
                    foretBoard.setBoard_hit(temp.getInt("board_hit"));
                    foretBoard.setBoard_like_count(temp.getInt("board_like_count"));
                    foretBoard.setBoard_comment_count(temp.getInt("board_comment_count"));
                    foretBoard.setBoard_writed_date(temp.getString("board_writed_date"));
                    foretBoard.setBoard_edited_date(temp.getString("board_edited_date"));

                    String photo_name = temp.getString("photo_name");
                    if (photo_name.equals("")) photo_name = "0";
                    foretBoard.setPhoto_name(photo_name);

                    // list에 저장
                    foretBoardList.add(foretBoard);
                    Log.d("[TEST]", "foretBoardList 통신 성공 => " + foretBoardList.size());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(MainActivity.this, "ForetBoard 통신 실패", Toast.LENGTH_SHORT).show();
        }
    }
}