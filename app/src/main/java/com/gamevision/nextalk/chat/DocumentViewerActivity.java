package com.gamevision.nextalk.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.gamevision.nextalk.R;

public class DocumentViewerActivity extends AppCompatActivity {
    private WebView webView;
    private String documentUrl;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_viewer);

        documentUrl = getIntent().getStringExtra("documentUrl");
        fileName = getIntent().getStringExtra("fileName");

        if (documentUrl == null) {
            finish();
            return;
        }

        setupWebView();
    }

    private void setupWebView() {
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient());

        // Load document based on type
        if (fileName.toLowerCase().endsWith(".pdf")) {
            webView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + documentUrl);
        } else {
            webView.loadUrl(documentUrl);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_document_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_download) {
            downloadDocument();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadDocument() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(documentUrl));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
} 