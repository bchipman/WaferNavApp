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
 {@link AssignHandlerFragment.OnFragmentInteractionListener} interface
 to handle interaction events.
 Use the {@link AssignHandlerFragment#newInstance} factory method to
 create an instance of this fragment.
 */
public class AssignHandlerFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mHandlerId;
    private String mHandlerLocation;
    private OnFragmentInteractionListener mListener;

    public AssignHandlerFragment() {
        // Required empty public constructor
    }

    public static AssignHandlerFragment newInstance(String param1, String param2) {
        AssignHandlerFragment fragment = new AssignHandlerFragment();
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
        View view = inflater.inflate(R.layout.fragment_assign_handler, container, false);

        TextView mTextViewHandler = (TextView) view.findViewById(R.id.textViewHandler);
        mTextViewHandler.append(": " + mHandlerId);

        // Set button handlers
        Button startDeliveryButton = (Button) view.findViewById(R.id.startDeliveryButton);
        startDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.startDeliveryButtonHandler(mHandlerId, mHandlerLocation);
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
        void startDeliveryButtonHandler(String id, String loc);
    }
}
