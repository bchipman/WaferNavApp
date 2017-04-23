package com.nielsenninjas.wafernav;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import com.nielsenninjas.wafernav.enums.Operation;

import java.util.HashSet;
import java.util.Set;

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
    private OnFragmentInteractionListener mListener;
    private AutoCompleteTextView mAutoCompleteTextViewBibIds;
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

        mAutoCompleteTextViewBibIds = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextViewBibIds);
        mBibIds = new HashSet<>();

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
        mBibIds.add(bibId);
    }

    public void setBibIdText(String bibId) {
        mAutoCompleteTextViewBibIds.setText(bibId);
    }
}
