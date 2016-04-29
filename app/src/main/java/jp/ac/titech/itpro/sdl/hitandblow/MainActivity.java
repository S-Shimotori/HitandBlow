package jp.ac.titech.itpro.sdl.hitandblow;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private final static int N = 4;

    private final static int[] GUESS_DIGITS = {
            R.id.guess_digit_0, R.id.guess_digit_1, R.id.guess_digit_2, R.id.guess_digit_3
    };
    private final static int[] NUM_KEYS = {
            R.id.num_key_1, R.id.num_key_2, R.id.num_key_3,
            R.id.num_key_4, R.id.num_key_5, R.id.num_key_6,
            R.id.num_key_7, R.id.num_key_8, R.id.num_key_9
    };

    private TextView messageView;
    private static final String KEY_MESSAGE_VIEW_TEXT = "MainActivity.messageView.text";
    private TextView[] guessDigits;
    private static final String KEY_GUESS_DIGITS_TEXTS = "MainActivity.guessDigits.texts";
    private ListView statusView;
    private Button[] numKeys;
    private Button ctrlButton;
    private static final String KEY_CTRL_BUTTON_TEXT = "MainActivity.ctrlButton.text";

    private ArrayList<String> status;
    private static final String KEY_STATUS = "MainActivity.status";
    private ArrayAdapter<String> statusAdapter;

    private int pos;
    private static final String KEY_POS = "MainActivity.pos";
    private int ntrial;
    private static final String KEY_N_TRIAL = "MainActivity.ntrial";
    private int[] problem;
    private static final String KEY_PROBLEM = "MainActivity.problem";
    private int[] guess;
    private static final String KEY_GUESS = "MainActivity.guess";
    private boolean game_started;
    private static final String KEY_GAME_STARTED = "MainActivity.game_started";

    private Random rnd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        messageView = (TextView) findViewById(R.id.message_view);
        if (savedInstanceState != null) {
            messageView.setText(savedInstanceState.getString(KEY_MESSAGE_VIEW_TEXT));
        }

        guessDigits = new TextView[GUESS_DIGITS.length];
        for (int i = 0; i < GUESS_DIGITS.length; i++) {
            guessDigits[i] = (TextView) findViewById(GUESS_DIGITS[i]);
            if (savedInstanceState != null) {
                guessDigits[i].setText(savedInstanceState.getStringArrayList(KEY_GUESS_DIGITS_TEXTS).get(i));
            }
        }
        statusView = (ListView) findViewById(R.id.status_view);
        numKeys = new Button[NUM_KEYS.length];
        for (int i = 0; i < NUM_KEYS.length; i++)
            numKeys[i] = (Button) findViewById(NUM_KEYS[i]);
        ctrlButton = (Button) findViewById(R.id.control_button);
        if (savedInstanceState != null) {
            ctrlButton.setText(savedInstanceState.getString(KEY_CTRL_BUTTON_TEXT));
        }

        status = new ArrayList<>();
        if (savedInstanceState != null) {
            status = savedInstanceState.getStringArrayList(KEY_STATUS);
        }
        statusAdapter = new ArrayAdapter<>(this, R.layout.status_item, status);
        statusView.setAdapter(statusAdapter);

        rnd = new Random();
        problem = new int[N];
        if (savedInstanceState != null) {
            problem = savedInstanceState.getIntArray(KEY_PROBLEM);
        }
        guess = new int[N];
        if (savedInstanceState != null) {
            guess = savedInstanceState.getIntArray(KEY_GUESS);
        }
        if (savedInstanceState == null) {
            initGame();
        } else {
            pos = savedInstanceState.getInt(KEY_POS);
            ntrial = savedInstanceState.getInt(KEY_N_TRIAL);
            game_started = savedInstanceState.getBoolean(KEY_GAME_STARTED);
        }
    }

    public void onClickNumKey(View v) {
        Log.d(TAG, "onClickNumKey");
        if (pos == 0)
            clearGuess();
        int d = 0;
        for (int i = 0; i < NUM_KEYS.length; i++)
            if (v.getId() == NUM_KEYS[i]) {
                d = i + 1;
                break;
            }
        // assert 1 <= d && d <= 9;
        for (int i = 0; i < pos; i++) {
            if (d == guess[i]) {
                Toast.makeText(this, getString(R.string.digit_already_used, d),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        guess[pos] = d;
        guessDigits[pos].setText(getString(R.string.guess_digit, d));
        pos++;
        if (pos == N) {
            ntrial++;
            int nh = 0, nb = 0;
            for (int i = 0; i < N; i++) {
                if (problem[i] == guess[i])
                    nh++;
                for (int j = 0; j < N; j++)
                    if (problem[i] == guess[j])
                        nb++;
            }
            nb -= nh;
            statusAdapter.add(getString(R.string.status_item, ntrial, a2i(guess), nh, nb));
            statusView.smoothScrollToPosition(statusAdapter.getCount() - 1);
            if (nh == N) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.you_win_title)
                        .setMessage(R.string.you_win_message)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                initGame();
                messageView.setText(getString(R.string.message_start));
                ctrlButton.setText(R.string.ctrl_start);
            }
            pos = 0;
        }
    }

    public void onClickCtrl(View v) {
        Log.d(TAG, "onClickCtrl");
        if (game_started) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.give_up_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            giveUpGame();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
        else
            startGame();
    }

    private void clearGuess() {
        for (TextView digit : guessDigits)
            digit.setText(R.string.guess_init_letter);
        for (int i = 0; i < guess.length; i++)
            guess[i] = 0;
    }

    private void initGame() {
        game_started = false;
        for (Button key : numKeys)
            key.setEnabled(false);
    }

    private void startGame() {
        newProblem();
        clearGuess();
        statusAdapter.clear();
        game_started = true;
        for (Button key : numKeys)
            key.setEnabled(true);
        ctrlButton.setText(R.string.ctrl_give_up);
        pos = 0;
        ntrial = 0;
        messageView.setText(getString(R.string.message_enter));
    }

    private void giveUpGame() {
        initGame();
        ctrlButton.setText(R.string.ctrl_start);
        messageView.setText(R.string.message_start);
    }

    private void newProblem() {
        for (int i = 0; i < N; i++) {
            int r;
            loop:
            while (true) {
                r = rnd.nextInt(9) + 1;
                for (int j = 0; j < i; j++)
                    if (r == problem[j]) continue loop;
                break;
            }
            problem[i] = r;
        }
        Log.i(TAG, "problem: " + a2i(problem));
    }

    private int a2i(int[] a) {
        int v = 0;
        for (int i : a)
            v = 10 * v + i;
        return v;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_MESSAGE_VIEW_TEXT, messageView.getText().toString());

        ArrayList<String> guessDigitsArray = new ArrayList();
        for(TextView textView: guessDigits) {
            guessDigitsArray.add(textView.getText().toString());
        }
        outState.putStringArrayList(KEY_GUESS_DIGITS_TEXTS, guessDigitsArray);

        outState.putString(KEY_CTRL_BUTTON_TEXT, ctrlButton.getText().toString());

        outState.putStringArrayList(KEY_STATUS, status);

        outState.putInt(KEY_POS, pos);

        outState.putInt(KEY_N_TRIAL, ntrial);

        outState.putIntArray(KEY_PROBLEM, problem);

        outState.putIntArray(KEY_GUESS, guess);

        outState.putBoolean(KEY_GAME_STARTED, game_started);
    }
}
