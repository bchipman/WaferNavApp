package com.nielsenninjas.wafernav;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 A simple {@link Fragment} subclass.
 Activities that contain this fragment must implement the
 {@link EnterStationIdFragment.OnFragmentInteractionListener} interface
 to handle interaction events.
 Use the {@link EnterStationIdFragment#newInstance} factory method to
 create an instance of this fragment.
 */
public class EnterStationIdFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mHandlerId;
    private String mHandlerLocation;
    private OnFragmentInteractionListener mListener;
    private TextView mTextViewStationId;


    public EnterStationIdFragment() {
        // Required empty public constructor
    }
    /**
     Use this factory method to create a new instance of
     this fragment using the provided parameters.

     @param param1 Parameter 1.
     @param param2 Parameter 2.

     @return A new instance of fragment EnterStationIdFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EnterStationIdFragment newInstance(String param1, String param2) {
        EnterStationIdFragment fragment = new EnterStationIdFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mHandlerId = getArguments().getString(ARG_PARAM1);
            mHandlerLocation = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_station_id, container, false);

        mTextViewStationId = (TextView) view.findViewById(R.id.textViewStationId);

        Button readBarcodeButton = (Button) view.findViewById(R.id.buttonReadBarcode);
        readBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.readStationBarcodeButtonHandler(mHandlerId, mHandlerLocation);
                }
            }
        });

        Button publishStationIdButton = (Button) view.findViewById(R.id.buttonPublishStationId);
        publishStationIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null && mTextViewStationId != null) {
                    String stationId = mTextViewStationId.getText().toString();
                    if (stationId != null && !stationId.isEmpty()) {
                        mListener.publishStationIdButtonHandler(stationId);
                    }
                    // TODO - Create toast message if stationId is null or empty
                }

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
        void readStationBarcodeButtonHandler(String id, String loc);
        void publishStationIdButtonHandler(String stationId);
    }


    public void setStationIdText(String stationId) {
        mTextViewStationId.setText(stationId);
    }
}