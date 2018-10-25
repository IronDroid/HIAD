package org.ajcm.hiad.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.ajcm.hiad.CallbackFragments;
import org.ajcm.hiad.R;
import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.models.Himno;
import org.ajcm.hiad.models.Himno1962;
import org.ajcm.hiad.models.Himno2008;

import java.util.ArrayList;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainFragment";
    private static final String KEY_VERSION = "key_version";
    private static final String KEY_NUMBER_INTEGER = "key_number_integer";
    private static final String KEY_NUMBER_STRING = "key_number_string";
    private static final int OLD_LIMIT = 527;
    private static final int NEW_LIMIT = 613;
    private int limit;
    private DBAdapter dbAdapter;
    private ArrayList<? extends Himno> listHimnos;
    private Himno himno;

    private TextView numberTextView;
    private TextView placeholderHimno;

    private boolean version2008 = true;
    private String numberString;
    private int numberInteger;

    private CallbackFragments callbackFragments;

    public MainFragment() {
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "onSaveInstanceState: " + numberInteger);
        Log.e(TAG, "onSaveInstanceState: " + numberString);
        outState.putInt(KEY_NUMBER_INTEGER, numberInteger);
        outState.putString(KEY_NUMBER_STRING, numberString);
        Log.e(TAG, "onSaveInstanceState: guardado");
    }

    public static MainFragment newInstance(boolean version2008) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_VERSION, version2008);
        Log.e(TAG, "newInstance: version: " + version2008);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        numberInteger = 0;
        numberString = "";
        dbAdapter = new DBAdapter(getContext());
        version2008 = getArguments().getBoolean(KEY_VERSION, true);
        listHimnos = dbAdapter.getAllHimno(version2008);
        if (version2008) {
            himno = new Himno2008();
            limit = NEW_LIMIT;
        } else {
            himno = new Himno1962();
            limit = OLD_LIMIT;
        }
        Log.e(TAG, "onCreate: " + listHimnos.size());
        dbAdapter.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initView(view);
        if (savedInstanceState != null) {
            numberInteger = savedInstanceState.getInt(KEY_NUMBER_INTEGER);
            numberString = savedInstanceState.getString(KEY_NUMBER_STRING);
            if (numberInteger > 0) {
                placeholderHimno.setText("");
                setTitleShow(numberInteger);
            }
        }
        Log.e(TAG, "onCreateView: " + savedInstanceState);
        Log.e(TAG, "onCreateView: " + numberInteger);

        String[] textos = getResources().getStringArray(R.array.textos_alabanza);
        int random = new Random().nextInt(textos.length);
        ((TextView) view.findViewById(R.id.texto_alabanza)).setText(textos[random]);

        return view;
    }

    private void initView(View view) {
        numberTextView = view.findViewById(R.id.number_himno);
        placeholderHimno = view.findViewById(R.id.placeholder_himno);
        Button one = view.findViewById(R.id.one);
        Button two = view.findViewById(R.id.two);
        Button three = view.findViewById(R.id.three);
        Button four = view.findViewById(R.id.four);
        Button five = view.findViewById(R.id.five);
        Button six = view.findViewById(R.id.six);
        Button seven = view.findViewById(R.id.seven);
        Button eight = view.findViewById(R.id.eight);
        Button nine = view.findViewById(R.id.nine);
        Button zero = view.findViewById(R.id.zero);
        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);
        five.setOnClickListener(this);
        six.setOnClickListener(this);
        seven.setOnClickListener(this);
        eight.setOnClickListener(this);
        nine.setOnClickListener(this);
        zero.setOnClickListener(this);
        ImageButton back_space = view.findViewById(R.id.back_space);
        back_space.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menosUno();
            }
        });
        back_space.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                cleanNum();
                return true;
            }
        });
        view.findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                okButton();
            }
        });
        setPlaceholderHimno();
    }

    private void cleanNum() {
        setPlaceholderHimno();
    }

    public void okButton() {
        Log.e(TAG, "okButton: " + numberInteger);
        if (numberInteger > 0) {
            Log.e(TAG, "okButton: " + himno.getNumero());
            callbackFragments.callbackOK(MainFragment.class, himno);
            setPlaceholderHimno();
        }
    }

    // se aumenta un digito al numero
    public void masUno(int num) {
        if (numberString.length() > 2) {
            return;
        }
        numberString = numberString + num;
        numberInteger = Integer.parseInt(numberString);
        Log.e(TAG, "masUno: " + numberInteger);
        Log.e(TAG, "masUno: " + numberString);
        Log.e(TAG, "masUno: " + numberString.length());
        if (numberInteger == 0) {
            numberString = "";
        }
        if (numberInteger > 0 && numberInteger <= limit ) {
            setTitleShow(numberInteger);
            placeholderHimno.setText("");
        } else {
            menosUno();
        }
    }

    // se quita un digito al numero
    public void menosUno() {
        if (numberString.length() > 0) {
            numberString = numberString.substring(0, numberString.length() - 1);
        }
        if (numberString.length() == 0) {
            setPlaceholderHimno();
            numberInteger = 0;
        } else {
            numberInteger = Integer.parseInt(numberString);
        }
        setTitleShow(numberInteger);
    }

    // placeholder del textview antes de digitar un numero
    private void setPlaceholderHimno() {
        numberInteger = 0;
        numberString = "";
        numberTextView.setText("");
        placeholderHimno.setText(version2008 ? R.string.placeholder_himno : R.string.placeholder_himno_old);
    }

    private void setTitleShow(int numero) {
        if (numero > 0) {
            himno = listHimnos.get(numero - 1);
            numberTextView.setText(himno.getNumero() + ". " + himno.getTitulo());
        } else {
            setPlaceholderHimno();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CallbackFragments) {
            callbackFragments = (CallbackFragments) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbackFragments = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.one:
                masUno(1);
                return;
            case R.id.two:
                masUno(2);
                return;
            case R.id.three:
                masUno(3);
                return;
            case R.id.four:
                masUno(4);
                return;
            case R.id.five:
                masUno(5);
                return;
            case R.id.six:
                masUno(6);
                return;
            case R.id.seven:
                masUno(7);
                return;
            case R.id.eight:
                masUno(8);
                return;
            case R.id.nine:
                masUno(9);
                return;
            case R.id.zero:
                masUno(0);
                return;
        }
    }
}
