package com.nielsenninjas.wafernav;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.nielsenninjas.wafernav.enums.Operation;

import java.util.HashMap;
import java.util.Map;

/**
 A simple {@link Fragment} subclass.
 Activities that contain this fragment must implement the
 {@link DeliveringToFragment.OnFragmentInteractionListener} interface
 to handle interaction events.
 Use the {@link DeliveringToFragment#newInstance} factory method to
 create an instance of this fragment.
 */
public class DeliveringToFragment extends Fragment {
    private static final String ARG_PARAM0 = "param0";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Operation mOperation;
    private String mHandlerId;
    private String mHandlerInfo;

    private OnFragmentInteractionListener mListener;

    public DeliveringToFragment() {
        // Required empty public constructor
    }

    public static DeliveringToFragment newInstance(Operation operation, String param1, String param2) {
        DeliveringToFragment fragment = new DeliveringToFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM0, operation);
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOperation = (Operation) getArguments().get(ARG_PARAM0);
            mHandlerId = getArguments().getString(ARG_PARAM1);
            mHandlerInfo = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivering_to, container, false);

        // Populate handler id into text view
        TextView textViewHandler = (TextView) view.findViewById(R.id.textViewHandlerId);
        textViewHandler.append(mHandlerId);

        // Parse and populate handler info into text views
        Map<Integer, TextView> handlerInfoMap = new HashMap<>();
        handlerInfoMap.put(0, (TextView) view.findViewById(R.id.textViewSiteName));
        handlerInfoMap.put(1, (TextView) view.findViewById(R.id.textViewSiteDescription));
        handlerInfoMap.put(2, (TextView) view.findViewById(R.id.textViewSiteLocation));
        String[] handlerInfoArr = mHandlerInfo.split(",");
        for (int i = 0; i < handlerInfoArr.length; i++) {
            handlerInfoMap.get(i).append(handlerInfoArr[i]);
        }

        // Set button handlers
        Button startDeliveryButton = (Button) view.findViewById(R.id.confirmDeliveryButton);
        startDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.confirmDeliveryButtonHandler(mHandlerId, mHandlerInfo);
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
        void confirmDeliveryButtonHandler(String id, String loc);
    }
}
