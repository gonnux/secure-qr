package com.gonnux.secureqr;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.gonnux.secureqr.databinding.ActivityMainBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 상태바 뒤가 Toolbar 색이라 아이콘 밝기를 모드별로 직접 정한다. 밤 모드는 밝은 보라(purple_200) 위라 어두운 아이콘.
        boolean night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        SystemBarStyle statusBar = night
                ? SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                : SystemBarStyle.dark(Color.TRANSPARENT);
        EdgeToEdge.enable(this, statusBar);
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // 상태바 inset 은 Toolbar 패딩으로 줘서 상태바 뒤까지 Toolbar 색이 이어지게 한다.
        // 테마 액션바를 쓰면 edge-to-edge 에서 콘텐츠가 상태바 높이만큼 액션바 밑으로 들어가 광고 배너가 가려졌다.
        ViewCompat.setOnApplyWindowInsetsListener(binding.container, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(insets.left, 0, insets.right, insets.bottom);
            binding.toolbar.setPadding(0, insets.top, 0, 0);
            return WindowInsetsCompat.CONSUMED;
        });

        binding.adView.loadAd(new AdRequest.Builder().build());

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_scanner, R.id.navigation_editor
        ).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
}