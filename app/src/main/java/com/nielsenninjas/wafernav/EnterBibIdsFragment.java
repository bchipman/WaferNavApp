package com.nielsenninjas.wafernav;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.nielsenninjas.wafernav.enums.Operation;

import java.util.HashSet;
import java.util.Set;

import static android.content.ContentValues.TAG;

/**
 A simple {@link Fragment} subclass.
 Activities that contain this fragment must implement the
 {@link EnterBibIdsFragment.OnFragmentInteractionListener} interface
 to handle interaction events.
 Use the {@link EnterBibIdsFragment#newInstance} factory method to
 create an instance of this fragment.
 */
public class EnterBibIdsFragment extends Fragment {

    private static final String TAG = "WNAV-EnterBibIdsFrag";

    private static final String ARG_PARAM0 = "param0";
    private static final String ARG_PARAM1 = "param1";
    private Operation mOperation;
    private String mHandlerId;
    private MainActivity mMainActivity;
    private OnFragmentInteractionListener mListener;
    private AutoCompleteTextView mAutoCompleteTextViewBibIds;
    private TextView mTextViewSubmissonHistory;
    private Set<String> mBibIds;

    public EnterBibIdsFragment() {
        // Required empty public constructor
    }

    public static EnterBibIdsFragment newInstance(Operation operation, String param1) {
        EnterBibIdsFragment fragment = new EnterBibIdsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM0, operation);
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOperation = (Operation) getArguments().get(ARG_PARAM0);
            mHandlerId = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_bib_ids, container, false);

        mMainActivity = (MainActivity) getActivity();
        mAutoCompleteTextViewBibIds = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextViewBibIds);
        mTextViewSubmissonHistory = (TextView) view.findViewById(R.id.textViewSubmissionHistory);
        mTextViewSubmissonHistory.setMovementMethod(new ScrollingMovementMethod());
        mBibIds = new HashSet<>();

        // Hide keyboard when (1) click non-EditText object, or (2) press enter in EditText object
        setupHideKeyboardListeners(view);

        Button readBarcodeButton = (Button) view.findViewById(R.id.buttonReadBarcode);
        readBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.readBarcodeButtonHandler(MainActivity.ENTER_BIB_IDS_BARCODE_CAPTURE);
                }
            }
        });

        Button addBibIdButton = (Button) view.findViewById(R.id.buttonAddBibId);
        addBibIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null && mAutoCompleteTextViewBibIds != null) {
                    String bibId = mAutoCompleteTextViewBibIds.getText().toString();
                    if (bibId != null && !bibId.isEmpty()) {
                        mListener.addBibIdButtonHandler(bibId);
                    }
                    // TODO - Create toast message if stationId is null or empty
                }
            }
        });

        Button startDeliveryButton = (Button) view.findViewById(R.id.startDeliveryButton);
        startDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null && !mBibIds.isEmpty()) {
                    mListener.startDeliveryButtonHandler(mHandlerId, mBibIds);
                }
                // TODO - Create toast message if no BIB ids
            }
        });

        return view;
    }

    private void hideKeyboard() {
        InputMethodManager inm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (getActivity().getCurrentFocus() != null) {
            inm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
        else {
            Log.w(TAG, "I WOULD HAVE CRASHED BECAUSE NOTHING IS FOCUSED!!");
        }
        getActivity().findViewById(R.id.parent).clearFocus();
    }

    private void setupHideKeyboardListeners(final View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard();
                    return false;
                }
            });
        }
        // Set up editor listener to hide keyboard when press enter in TextEdit object
        else {
            ((EditText) view).setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        hideKeyboard();
                        // if this is mAutoCompleteTextViewBibIds, also trigger the 'Add BIB ID' button when press enter
                        if (view == mAutoCompleteTextViewBibIds) {
                            mListener.addBibIdButtonHandler(mAutoCompleteTextViewBibIds.getText().toString());
                        }
                    }
                    return false;
                }
            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupHideKeyboardListeners(innerView);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     This interface must be implemented by activities that contain this
     fragment to allow an interaction in this fragment to be communicated
     to the activity and potentially other fragments contained in that
     activity.
     See the Android Training lesson <a href=
     "http://developer.android.com/training/basics/fragments/communicating.html"
     >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void readBarcodeButtonHandler(int barcodeCaptureId);
        void addBibIdButtonHandler(String bibId);
        void startDeliveryButtonHandler(String bluId, Set<String> bibIds);
    }

    public void addBibId(String bibId) {
        Log.i(TAG, "addBibId");
        mAutoCompleteTextViewBibIds.setText(null);

        final int bibIdsSizeBefore = mBibIds.size();
        mBibIds.add(bibId);
        // Don't add bib id to text box if not added to set
        if (mBibIds.size() == bibIdsSizeBefore) {
            mMainActivity.makeShortToast("BIB ID already added!");
            return;
        }
        mMainActivity.makeShortToast("Added BIB ID " + bibId);

        // Append new line at start if not the first line
        if (mTextViewSubmissonHistory.getText().length() == 0) {
            mTextViewSubmissonHistory.append(bibId);
        } else {
            mTextViewSubmissonHistory.append("\n" + bibId);
        }

        // Keep text view scrolled to bottom
        final int scrollAmount = mTextViewSubmissonHistory.getLayout().getLineTop(mTextViewSubmissonHistory.getLineCount()) - mTextViewSubmissonHistory.getHeight();
        if (scrollAmount > 0) {
            mTextViewSubmissonHistory.scrollTo(0, scrollAmount);
        } else {
            mTextViewSubmissonHistory.scrollTo(0, 0);
        }

    }

    public void setBibIdText(String bibId) {
        mAutoCompleteTextViewBibIds.setText(bibId);
    }
}
