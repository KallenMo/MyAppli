package com.example.myappli;

import android.app.Application;

/**
 * Description:
 * Detail:
 * Create Time: 2020/3/11
 *
 * @author kallen
 * @version 1.0
 * @see ...
 * History:
 * @since Since
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppContainer.put(this);
    }
}
