package gs.ibeacon.fcu.slideswipe.Fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.sails.engine.LocationRegion;

import java.util.List;

import gs.ibeacon.fcu.slideswipe.*;
import gs.ibeacon.fcu.slideswipe.Log.DLog;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GuideFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GuideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GuideFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String TAG = "GuideFragment";
    private AlertDialog.Builder locationListDialog;
    private Button locationListButton;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public GuideFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GuideFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GuideFragment newInstance(String param1, String param2) {
        GuideFragment fragment = new GuideFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DLog.d(TAG, "onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.title_guide);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_guide, container, false);



        final ArrayAdapter<String> locationListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_selectable_list_item);
        List<LocationRegion> l = MainActivity.mainActivity.getSails().getLocationRegionList("1");
        try {
            for (int i = 0; i < l.size(); i++) {
                String newLocationl = l.get(i).label;
                if (!newLocationl.equals("")) {
                    locationListAdapter.add(newLocationl);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        locationListDialog = new AlertDialog.Builder(getActivity());
        locationListDialog.setTitle("地點導引");
        locationListDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    List<LocationRegion> l = MainActivity.mainActivity.getSails().getLocationRegionList("1");
                    for (int i = 0; i < l.size(); i++) {
                        String newLocationl = l.get(i).label;
                        if (!newLocationl.equals("")) {
                            locationListAdapter.add(newLocationl);
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        MaterialDialog m = new MaterialDialog(getActivity());
        locationListDialog.setAdapter(locationListAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String guideLocation = locationListAdapter.getItem(which);
                //String friendLocation = friendLocList.get(friendNameList.indexOf(friendName));
                MainActivity.mainActivity.guideToTarget(guideLocation, 1);
                ( (TextView) (v.findViewById(R.id.guideLocationText))).setText("目的地 : " + guideLocation);
            }
        });

     //   locationListDialog.show();

        locationListButton = (Button) v.findViewById(R.id.buttonLocationList);
        locationListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationListDialog.show();
            }
        });
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
