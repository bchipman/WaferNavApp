package com.nielsenninjas.wafernav;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.vision.text.Text;
import com.nielsenninjas.wafernav.enums.Operation;

import static android.content.ContentValues.TAG;

/**
 A simple {@link Fragment} subclass.
 Activities that contain this fragment must implement the
 {@link EnterStationIdFragment.OnFragmentInteractionListener} interface
 to handle interaction events.
 Use the {@link EnterStationIdFragment#newInstance} factory method to
 create an instance of this fragment.
 */
public class EnterStationIdFragment extends Fragment {
    private static final String ARG_PARAM0 = "param0";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Operation mOperation;
    private String mStationType;
    private String mButtonText;
    private OnFragmentInteractionListener mListener;
    private AutoCompleteTextView mAutoCompleteTextViewStationId;

    public EnterStationIdFragment() {
        // Required empty public constructor
    }

    public static EnterStationIdFragment newInstance(Operation operation, String stationType, String buttonText) {
        EnterStationIdFragment fragment = new EnterStationIdFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM0, operation);
        args.putString(ARG_PARAM1, stationType);
        args.putString(ARG_PARAM2, buttonText);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOperation = (Operation) getArguments().get(ARG_PARAM0);
            mStationType = getArguments().getString(ARG_PARAM1);
            mButtonText = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_station_id, container, false);

        mAutoCompleteTextViewStationId = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextViewStationId);

        TextView scanStationTextView = (TextView) view.findViewById(R.id.scanStation);
        scanStationTextView.setText("Scan " + mStationType + " Station Barcode");

        // Hide keyboard when (1) click non-EditText object, or (2) press enter in EditText object
        setupHideKeyboardListeners(view);

        Button readBarcodeButton = (Button) view.findViewById(R.id.buttonReadBarcode);
        readBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.readBarcodeButtonHandler(MainActivity.ENTER_STATION_BARCODE_CAPTURE);
                }
            }
        });

        Button publishStationIdButton = (Button) view.findViewById(R.id.buttonPublishStationId);
        if (mButtonText != null) {
            publishStationIdButton.setText(mButtonText);
        }
        publishStationIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null && mAutoCompleteTextViewStationId != null) {
                    String stationId = mAutoCompleteTextViewStationId.getText().toString();
                    if (stationId != null && !stationId.isEmpty()) {
                        mListener.publishStationIdButtonHandler(stationId);
                    }
                    // TODO - Create toast message if stationId is null or empty
                }
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
                        // if this is mAutoCompleteTextViewStationId, also trigger the 'Submit Barcode ID' button when press enter
                        if (view == mAutoCompleteTextViewStationId) {
                            mListener.publishStationIdButtonHandler(mAutoCompleteTextViewStationId.getText().toString());
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
        void publishStationIdButtonHandler(String stationId);
    }

    public void setStationIdText(String stationId) {
        mAutoCompleteTextViewStationId.setText(stationId);
    }
}
