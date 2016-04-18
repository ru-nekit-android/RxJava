package ru.nekit.android.rxjava;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (false) {

            Observable.just("Hello, world!")
                    .subscribe(s -> Log.v("ru.nekit.android.rx", s));
            //
            Observable.just("Hello, world!").map(s -> s + s).map(p -> "(" + p + ")").subscribe(s -> Log.v("ru.nekit.android.rx", s));
            //
            Observable.just("10").map(String::hashCode).map(Object::toString).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()).subscribe(this::log);
            //
            query("android").subscribe(
                    urls -> Observable.from(urls).map(url -> "+" + url).subscribe(this::log)
            );
            //
            query("android").flatMap(Observable::from).filter(url -> url != null).map(url -> "Title_" + url + ":" + url).flatMap(this::getTitle).doOnNext(url -> log("Save:" + url))
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(Schedulers.io())
                    .subscribe(this::log);
            //
            Observable<OnTextChangeEvent> observable = WidgetObservable.text((TextView) findViewById(R.id.inputField), false);
            observable
                    .debounce(500, TimeUnit.MILLISECONDS)
                    .map(input -> input.text().toString())
                    .filter(input -> input.length() > 3)
                /*.observeOn(Schedulers.newThread())
                .map(value -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException exp) {
                        //
                    }
                    return value;
                })*/
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(((TextView) findViewById(R.id.outputField))::setText);
            //observableFilter.doOnNext(null);

            //
            Observable.from(Arrays.asList(1, 1, 3, 3, 3, 6, 7, 8))
                    //.filter(value -> value % 2 == 0)
                    .map(value -> value)
                    .map(Math::sqrt).
                    map(Object::toString).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).onErrorReturn(throwable -> {
                log(Arrays.toString(throwable.getStackTrace()).replace(",", "\n"));
                return null;
            })
                    .distinctUntilChanged()
                    .subscribe(this::log);
            //
            Observable<String> s1 = Observable.create(subscriber -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException exp) {
                    //
                }
                subscriber.onNext("Complete computation");
                subscriber.onCompleted();

            });
            Observable<String> s2 = Observable.create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {
                    subscriber.onNext("data");
                    subscriber.onCompleted();
                }
            });
            s1.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.handlerThread(new Handler(Looper.getMainLooper()))).subscribe(((TextView) findViewById(R.id.outputField2))::setText);
            s2.subscribe(this::log);
            //
            Observable<Integer> o = Observable.just(0);
            o.map(value -> 1 / value).map(Object::toString)
                    .onErrorReturn(s -> {
                        log("Error: " + s.getMessage());
                        return null;
                    })
                    .retry(3)
                    .subscribe(this::log);
            //
            //Random rand = new Random();
            //Observable<Object> or = Observable.just(rand.nextInt(2))
            //       .flatMap(in -> in == 1 ? Observable.just("One") : Observable.just(1));
            //int r = 0;
            ///Observable.range(0, 1000000).observeOn(Schedulers.from(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))).subscribeOn(AndroidSchedulers.mainThread()).map(Object::toString).subscribe(this::log);


            Observable.zip(Observable.range(2, 10), Observable.range(11, 20), (integer1, integer2) -> {
                ArrayList<Integer> integers = new ArrayList<>();
                integers.add(integer1);
                integers.add(integer2);
                return integers;
            }).subscribe(value -> log("[]: " + Arrays.toString(value.toArray())));

            Observable.concat(Observable.range(2, 10), Observable.range(11, 20)).subscribe(value -> log("[0]:" + value));


            Observable.range(0, 10).replay().subscribe(value -> log("[!]:" + value));
        }

        Func1<Integer, Observable<Integer>> t = value -> Observable.just(value).filter(v -> v != null).doOnNext(integer -> log("CALL: " + integer));
        //Observable.combineLatest(t.call(null), t.call(2), null).map(Object::toString).subscribe(this::log);

        ConnectableObservable cob = Observable.fromCallable(() -> t.call(1)).publish();


        cob.connect();
    }

    private Observable<String> getTitle(String s) {
        return Observable.just(s.split(":")[0]);
    }

    private void log(String value) {
        Log.v("ru.nekit.android.rx", value);
    }


    Observable<List<String>> query(String url) {
        return Observable.just(Arrays.asList(url + "/1", url + "/2", url + "/3", null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
