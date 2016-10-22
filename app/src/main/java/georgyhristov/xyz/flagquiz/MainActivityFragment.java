package georgyhristov.xyz.flagquiz;


import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import android.os.Handler;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;


/**
 * A placeholder fragment containing Flag Quiz Logic
 */

public class MainActivityFragment extends Fragment {
    private static final String TAG = MainActivityFragment.class.getSimpleName();

    private static final int FLAGS_IN_QUIZ = 10;

    private List<String> fileNameList; // flag file names
    private List<String> quizCountriesList; //countries in the current list;
    private Set<String> regionsSet; //world regions in the current quiz
    private String correctAnswer; //correct country for the current flag
    public static int totalGuesses; // number of guesses made
    private int correctAnswers; // number of correct guesses
    private int guessRows; //number of rows displaying guess Buttons
    private SecureRandom random; //randomize the quiz
    private android.os.Handler handler; // delay loading the next flag!
    private Animation shakeAnimation; // incorrect guess shake

    private LinearLayout quizLinearLayout; //contains the quiz
    private TextView questionNumberTextView; // shows the correct question
    private ImageView flagImageView; //display a flag
    private LinearLayout[] guessLinearLayouts; //rows of answer buttons
    private TextView answerTextView; // displays the correct answer
    private View view;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_main, container, false);

        fileNameList = new ArrayList<>();
        quizCountriesList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();


        //load the shake animation:
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3); //repeat 3 times

        //get preferences to GUI components

        quizLinearLayout = (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] = (LinearLayout) view.findViewById(R.id.row4LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        //configure the listeners for the buttons:

        for (LinearLayout row : guessLinearLayouts) {
            for (int col = 0; col < row.getChildCount(); col++) {
                Button button = (Button) row.getChildAt(col);
                button.setOnClickListener(guessButtonListener);
            }
        }

        //set questionNumberText view:
        questionNumberTextView.setText(getString(R.string.question,
                1, FLAGS_IN_QUIZ));
        return view; // return the created view !!!!
    }

    public void updateGuessRows(SharedPreferences sharedPreferences) {
        //get the number of guess buttons that should be displayed
        String choices = sharedPreferences.getString(MainActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        //hide all guess button LinearLayouts
        for (LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.GONE);

        //display appropriate guess button LinearLayouts
        for (int row = 0; row < guessRows; row++)
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
    }

    public void updateRegions(SharedPreferences sharedPreferences) {
        //update world regions for the quiz based on the sharedPreferences
        regionsSet = sharedPreferences.getStringSet(MainActivity.REGIONS, null);
    }

    //set up and start the next quiz
    public void resetQuiz() {
        //AssetManager to get image file names for enables regions
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear();//empty the list of image names
        try {
            //loop over each region
            for (String region : regionsSet) {
                //get a list of all flag image files in this region

                String[] paths = assets.list(region);

                for (String path : paths) {
                    fileNameList.add(path.replace(".png", ""));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "resetQuiz: Error loading image file names", e);
        }

        correctAnswers = 0; // reset the number of correct answers
        totalGuesses = 0; // reset the total number of guesses
        quizCountriesList.clear(); // clear list of quiz countries

        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();

        //add FLAGS_IN_QUIZ random file names to the quizCountriesList
        while (flagCounter <= FLAGS_IN_QUIZ) {
            int randomIndex = random.nextInt(numberOfFlags);

            //get the random file name
            String fileName = fileNameList.get(randomIndex);

            //if the region is enabled and it hasnt already been chosen
            if (!quizCountriesList.contains(fileName)) {
                quizCountriesList.add(fileName); // add the file to the list
                ++flagCounter;
            }
        }
        loadNextFlag();
    }

    //load next flag after a correct guess)
    private void loadNextFlag() {
        //get the file name of the correct flag and remove it from the list
        String nextImage = quizCountriesList.remove(0);
        correctAnswer = nextImage; // update the correct answer
        answerTextView.setText(""); // clear the text view

        //display current question number
        questionNumberTextView.setText(getString(R.string.question, (correctAnswers + 1), FLAGS_IN_QUIZ));

        //extract the region from the next image name
        String region = nextImage.substring(0, nextImage.indexOf('-'));

        //use asset manager to load next image from assets folder
        AssetManager assets = getActivity().getAssets();

        //get an InputStream to the asses representing the next flag
        //and try to use the InputStream
        try {
            try (InputStream stream =
                         assets.open(region + "/" + nextImage + ".png")) {
                //load the asset as a Drawable and display on the flagImageView
                Drawable flag = Drawable.createFromStream(stream, nextImage);
                flagImageView.setImageDrawable(flag);

                // nextFlagAnimation(false); //nextFlagAnimation the flags on to the screen
            }
        } catch (IOException e) {
            Log.e(TAG, "loadNextFlag: Error loading" + nextImage, e);
        }
        Collections.shuffle(fileNameList);//everyday I am shufflin'

        //put the correct answer at the end of fileNameList
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        //add 2, 4, 6 or 8 guess buttons based on the value of guessRows
        for (int row = 0; row < guessRows; row++) {
            //place Buttons in currentTableTow
            for (int col = 0; col < guessLinearLayouts[row].getChildCount(); col++) {
                //get reference to button to configure
                Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(col);
                newGuessButton.setEnabled(true);
                newGuessButton.setVisibility(View.VISIBLE);

                //get country name and set it as newGuessButton's text
                String fileName = fileNameList.get((row * 2) + col);
                newGuessButton.setText(getCountryName(fileName));
            }
        }
        //randomly replace one Button with the correct answer
        int row = random.nextInt(guessRows);//pick a random row
        int col = random.nextInt(2); //pick random col
        LinearLayout randomRow = guessLinearLayouts[row];//get the row
        String countryName = getCountryName(correctAnswer);
        ((Button) randomRow.getChildAt(col)).setText(countryName);
    }


    //parse(reads) the country flag file name and returns it
    private String getCountryName(String fileName) {
        return fileName.substring(fileName.indexOf('-') + 1).replace('_', ' ');
    }

    //called when a guess button is touched
    public View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            String answer = getCountryName(correctAnswer);
            totalGuesses++; //increment the number of guesses

            if (guess.equals(answer)) {
                ++correctAnswers;
                correctAnimation();
                //display correct answer in green
                answerTextView.setText(answer + "!");
                String color = "#00CC00";
                answerTextView.setTextColor(Color.parseColor(color));
                disableButtons();


                //if the user has correctly identified FLAG
                if (correctAnswers == FLAGS_IN_QUIZ) {
                    //Dialog Fragment to display quiz stats and start a new quiz
                    showPoints();
                } else {
                    //answer is correct but the quiz is not over
                    //load the next flag after 2 sec delay
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            nextFlagAnimation();
                            loadNextFlag();
                        }
                    }, 2000);
                }
            } else {// incorrect answer!
                incorrectAnimation();
                //display incorrect in red
                answerTextView.setText(R.string.incorrect_answer);
                String color = "#FF0000";
                answerTextView.setTextColor(Color.parseColor(color));
                guessButton.setEnabled(false);
                guessButton.setVisibility(View.INVISIBLE);
            }
        }
    };

    private void disableButtons() {
        for (int row = 0; row < guessRows; row++) {
            LinearLayout guessRow = guessLinearLayouts[row];
            for (int i = 0; i < guessRow.getChildCount(); i++)
                guessRow.getChildAt(i).setEnabled(false);
        }
    }

    private void showPoints() {
        FinishDialog dialog = new FinishDialog();
        dialog.show(getFragmentManager(), "what_is_this_for");
    }


    //animations start below:
    private void nextFlagAnimation() {
        YoYo.with(Techniques.BounceIn)
                .duration(700)
                .playOn(view.findViewById(R.id.flagImageView));
    }

    private void incorrectAnimation() {
        YoYo.with(Techniques.Wobble)
                .duration(700)
                .playOn(view.findViewById(R.id.flagImageView));
    }

    private void correctAnimation() {
        YoYo.with(Techniques.Hinge)
                .duration(700)
                .playOn(view.findViewById(R.id.flagImageView));
    }


}
