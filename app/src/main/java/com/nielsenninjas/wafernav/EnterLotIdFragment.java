package com.nielsenninjas.wafernav;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

/**
 A simple {@link Fragment} subclass.
 Activities that contain this fragment must implement the
 {@link EnterLotIdFragment.OnFragmentInteractionListener} interface
 to handle interaction events.
 Use the {@link EnterLotIdFragment#newInstance} factory method to
 create an instance of this fragment.
 */
public class EnterLotIdFragment extends Fragment {

    private static final String TAG = "EnterIdFragment";

    private OnFragmentInteractionListener mListener;

    // UI elements
    protected AutoCompleteTextView mAutoCompleteTextViewId;

    public EnterLotIdFragment() {
        // Required empty public constructor
    }

    public static EnterLotIdFragment newInstance() {
        return new EnterLotIdFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_enter_lot_id, container, false);

        // Hide keyboard when (1) click non-EditText object, or (2) press enter in EditText object
        setupHideKeyboardListeners(view);

        // AutoCompleteTextView for IDs
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.ids, android.R.layout.simple_dropdown_item_1line);
        mAutoCompleteTextViewId = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextViewLotId);
        mAutoCompleteTextViewId.setAdapter(adapter);

        // Focus publish button when start app
        view.findViewById(R.id.buttonPublish).requestFocus();
        hideKeyboard();


        // Set button handlers
        Button publishButton = (Button) view.findViewById(R.id.buttonPublish);
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.publishButtonHandler(mAutoCompleteTextViewId.getText().toString());
                }
            }
        });

        Button readBarcodeButton = (Button) view.findViewById(R.id.buttonReadBarcode);
        readBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.readBarcodeButtonHandler();
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
                        // if this is mEditTextId, also trigger the 'Publish' button when press enter
                        if (view == mAutoCompleteTextViewId) {
                            mListener.publishButtonHandler(mAutoCompleteTextViewId.getText().toString());
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
        void publishButtonHandler(String lotId);
        void readBarcodeButtonHandler();
    }

    public void setLotIdText(String lotId) {
        mAutoCompleteTextViewId.setText(lotId);
    }
}
