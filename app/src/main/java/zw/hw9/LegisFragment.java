package zw.hw9;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import zw.hw9.zw.hw9.models.LegislatorModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class LegisFragment extends Fragment implements TabHost.OnTabChangeListener,View.OnClickListener {
    private RelativeLayout layout;
    private String[] stateUrl = {"http://congressinfo.us-west-2.elasticbeanstalk.com/?symbol=AAPL"};
    private String[] houseUrl = {"http://congressinfo.us-west-2.elasticbeanstalk.com/?symbol=house"};
    private String[] senateUrl ={"http://congressinfo.us-west-2.elasticbeanstalk.com/?symbol=senate"};
    ListView mainList;
    ListView houseList;
    ListView senateList;
    TabHost tbHost;
    private String[] texts ={"TEST1","TEST2"};
    Map<String,Integer> map;
    Map<String,Integer> houseMap;
    Map<String,Integer> senateMap;
    public LegisFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout=(RelativeLayout) inflater.inflate(R.layout.fragment_legis,container,false);
         tbHost =(TabHost)layout.findViewById(R.id.tabHost);
        tbHost.setup();

        TabHost.TabSpec spec = tbHost.newTabSpec("Tab One");
        spec.setContent(R.id.tab1);
        spec.setIndicator("BY STATES");
        tbHost.addTab(spec);

        //Tab 2
        spec = tbHost.newTabSpec("Tab Two");
        spec.setContent(R.id.tab2);
        spec.setIndicator("HOUSE");
        tbHost.addTab(spec);

        //Tab 3
        spec = tbHost.newTabSpec("Tab Three");
        spec.setContent(R.id.tab3);
        spec.setIndicator("SENATE");
        tbHost.addTab(spec);
        tbHost.setCurrentTab(0);
        tbHost.setOnTabChangedListener(this);

        mainList = (ListView)layout.findViewById(R.id.listviewstate);
        houseList =(ListView)layout.findViewById(R.id.listviewhouse);
        senateList=(ListView)layout.findViewById(R.id.listviewsenate);
        mainList.setOnItemClickListener(onListClick);
        houseList.setOnItemClickListener(onListClick);
        senateList.setOnItemClickListener(onListClick);

        new LegTask().execute(stateUrl);
        new LegHouseTask().execute(houseUrl);
        new LegSenateTask().execute(senateUrl);
        return layout;
    }
       private void getIndex(List<LegislatorModel> list){
        map = new LinkedHashMap<>();
        for(int i =0;i<list.size();i++){
            LegislatorModel temp = list.get(i);
            String index = temp.getState().substring(0,1).toUpperCase();
            if(map.get(index)==null) {
                map.put(index, i);

            }
        }
    }

    private void getHouseIndex(List<LegislatorModel> list){
        houseMap = new LinkedHashMap<>();
        for(int i =0;i<list.size();i++){
            LegislatorModel temp = list.get(i);
            String index = temp.getName().substring(0,1).toUpperCase();
            if(houseMap.get(index)==null) {
                houseMap.put(index, i);
            }
        }
    }
    private void getSenateIndex(List<LegislatorModel> list){
        senateMap = new LinkedHashMap<>();
        for(int i =0;i<list.size();i++){
            LegislatorModel temp = list.get(i);
            String index = temp.getName().substring(0,1).toUpperCase();
            if(senateMap.get(index)==null) {
                senateMap.put(index, i);
            }
        }
    }

    private void displayIndex(LinearLayout sideLayout){

        TextView tv;
        Map<String,Integer> temp = null;

        if( sideLayout.getId()==R.id.stateside){
            temp = map;
        }
        if(sideLayout.getId()==R.id.houseside){
            temp = houseMap;
        }
        if(sideLayout.getId()==R.id.senateside){
            temp = senateMap;
        }
        List<String> sideIndex = new ArrayList<>(temp.keySet());
        for(String index: sideIndex){
            tv = (TextView) getActivity().getLayoutInflater().inflate(R.layout.side_index_item,null);
            tv.setText(index);
            tv.setOnClickListener(this);
            sideLayout.addView(tv);
        }
    }
    public void onClick(View view){
        TextView selected = (TextView)view;
        if(tbHost.getCurrentTab()==0) {
            mainList.setSelection(map.get(selected.getText()));
        }

        if(tbHost.getCurrentTab()==1) {
            houseList.setSelection(houseMap.get(selected.getText()));
        }
        if(tbHost.getCurrentTab()==2) {
            senateList.setSelection(senateMap.get(selected.getText()));
        }
    }
    private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            Intent intent = new Intent(getActivity(),legDetailActivity.class);
            LegislatorModel lm =(LegislatorModel) adapterView.getAdapter().getItem(i);
            intent.putExtra("leg",lm);
            startActivity(intent);
        }
    };
    class LegTask extends AsyncTask<String,String,List<LegislatorModel>>{

        BufferedReader reader;
        HttpURLConnection connection;
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected List<LegislatorModel> doInBackground(String... urls) {
            List<LegislatorModel> lModelList = new ArrayList<>();
            try {
                URL url = new URL(urls[0]);

                connection =(HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                String line="";
                StringBuffer buffer = new StringBuffer();
                while((line = reader.readLine())!=null){
                    buffer.append(line);
                }

                JSONArray parentArray = new JSONObject(buffer.toString()).getJSONArray("results");

                for(int i=0;i<parentArray.length();i++){
                    LegislatorModel lModel = new LegislatorModel();

                    JSONObject results = parentArray.getJSONObject(i);
                    String party = results.getString("party");
                    String bioguide_id = results.getString("bioguide_id");
                    String name = results.getString("last_name")+", "+results.getString("first_name");
                    String district = results.getString("district");
                    if(district.equals("null")) district="0";
                    String state = results.getString("state_name");
                    lModel.setBioguide_id(bioguide_id);
                    lModel.setDistrict(district);
                    lModel.setParty(party);
                    lModel.setName(name);
                    lModel.setState(state);
                    lModel.setFullname(results.getString("title")+". "+name);
                    String email =results.getString("oc_email");
                    lModel.setChamber(results.getString("chamber"));
                    lModel.setEmail(email.equals("null")?"N.A.":email);
                    lModel.setContact(results.getString("phone"));
                    lModel.setStartTerm(results.getString("term_start"));
                    lModel.setEndTerm(results.getString("term_end"));
                    lModel.setOffice(results.getString("office"));
                    lModel.setSt(results.getString("state"));
                    lModel.setFax(results.getString("fax").equals("null")?"N.A.":results.getString("fax"));
                    lModel.setBirth(results.getString("birthday"));
                    lModel.setTitle(results.getString("title"));
                    if(results.has("twitter_id")&&!results.isNull("twitter_id")){
                        lModel.setTwitter(results.getString("twitter_id"));
                    }else{
                        lModel.setTwitter("None");
                    }
                    if(results.has("facebook_id")&&!results.isNull("facebook_id")){
                        lModel.setFacebook(results.getString("facebook_id"));
                    }else{
                        lModel.setFacebook("None");
                    }
                    if(results.has("website")&&!results.isNull("website")){
                        lModel.setWebsite(results.getString("website"));
                    }else{
                        lModel.setWebsite("None");
                    }
                    lModelList.add(lModel);
                }
                //publishProgress(buffer.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection!=null) {
                    connection.disconnect();
                }
                try {
                    if(reader!=null) reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return lModelList;
        }

        @Override
        protected void onProgressUpdate(String... values) {


        }

        @Override
        protected void onPostExecute(List<LegislatorModel> result) {
            //TODO need to set data to the list
            Activity myActivity = getActivity();
            if (myActivity==null) return;
            LegislatorAdapter adapter = new LegislatorAdapter(myActivity.getApplicationContext(),R.layout.legrow,result);
            adapter.sort(new Comparator<LegislatorModel>() {
                @Override
                public int compare(LegislatorModel l1, LegislatorModel l2) {
                    return l1.getState().compareTo(l2.getState());
                }
            });
            mainList.setAdapter(adapter);
            getIndex(adapter.legList);
            LinearLayout sideLayout = (LinearLayout)getActivity().findViewById(R.id.stateside);
            displayIndex(sideLayout);

        }

    }
    class LegHouseTask extends AsyncTask<String,String,List<LegislatorModel>>{
        //ArrayAdapter<String> adapter;
        BufferedReader reader;
        HttpURLConnection connection;
        @Override
        protected void onPreExecute() {
            //adapter =(ArrayAdapter<String>)mainList.getAdapter();
        }

        @Override
        protected List<LegislatorModel> doInBackground(String... urls) {
            List<LegislatorModel> lModelList = new ArrayList<>();
            for(String item:texts){
                //publishProgress(item);
            }
            try {
                URL url = new URL(urls[0]);

                connection =(HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                String line="";
                StringBuffer buffer = new StringBuffer();
                while((line = reader.readLine())!=null){
                    buffer.append(line);
                }
                String finalJson= buffer.toString();
                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("results");

                for(int i=0;i<parentArray.length();i++){
                    LegislatorModel lModel = new LegislatorModel();

                    JSONObject results = parentArray.getJSONObject(i);
                    String party = results.getString("party");
                    String bioguide_id = results.getString("bioguide_id");
                    String name = results.getString("last_name")+", "+results.getString("first_name");
                    String district = results.getString("district");
                    if(district.equals("null")) district="0";
                    String state = results.getString("state_name");
                    lModel.setBioguide_id(bioguide_id);
                    lModel.setDistrict(district);
                    lModel.setParty(party);
                    lModel.setName(name);
                    lModel.setState(state);
                    lModel.setFullname(results.getString("title")+". "+name);
                    String email =results.getString("oc_email");
                    lModel.setChamber(results.getString("chamber"));
                    lModel.setEmail(email.equals("null")?"N.A.":email);
                    lModel.setContact(results.getString("phone"));
                    lModel.setStartTerm(results.getString("term_start"));
                    lModel.setEndTerm(results.getString("term_end"));
                    lModel.setOffice(results.getString("office"));
                    lModel.setSt(results.getString("state"));
                    lModel.setFax(results.getString("fax").equals("null")?"N.A.":results.getString("fax"));
                    lModel.setTitle(results.getString("title"));
                    lModel.setBirth(results.getString("birthday"));

                    if(results.has("twitter_id")&&!results.isNull("twitter_id")){
                        lModel.setTwitter(results.getString("twitter_id"));
                    }else{
                        lModel.setTwitter("None");
                    }
                    if(results.has("facebook_id")&&!results.isNull("facebook_id")){
                        lModel.setFacebook(results.getString("facebook_id"));
                    }else{
                        lModel.setFacebook("None");
                    }
                    if(results.has("website")&&!results.isNull("website")){
                        lModel.setWebsite(results.getString("website"));
                    }else{
                        lModel.setWebsite("None");
                    }

                    lModelList.add(lModel);
                }
                //publishProgress(buffer.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection!=null) {
                    connection.disconnect();
                }
                try {
                    if(reader!=null) reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return lModelList;
        }

        @Override
        protected void onProgressUpdate(String... values) {


        }

        @Override
        protected void onPostExecute(List<LegislatorModel> result) {
            //TODO need to set data to the list
            Activity myActivity = getActivity();
            if (myActivity==null) return;
            LegislatorAdapter adapter = new LegislatorAdapter(myActivity.getApplicationContext(),R.layout.legrow,result);
            adapter.sort(new Comparator<LegislatorModel>() {
                @Override
                public int compare(LegislatorModel l1, LegislatorModel l2) {
                    String s1 =l1.getName(), s2 = l2.getName();
                    int i1 =s1.indexOf(','), i2 = s2.indexOf(',');
                    return s1.substring(0,i1).compareTo(s2.substring(0,i2));

                }
            });
            houseList.setAdapter(adapter);
            getHouseIndex(adapter.legList);
            LinearLayout sideLayout = (LinearLayout)getActivity().findViewById(R.id.houseside);
            displayIndex(sideLayout);

        }

    }
    class LegSenateTask extends AsyncTask<String,String,List<LegislatorModel>>{
        //ArrayAdapter<String> adapter;
        BufferedReader reader;
        HttpURLConnection connection;
        @Override
        protected void onPreExecute() {
            //adapter =(ArrayAdapter<String>)mainList.getAdapter();
        }

        @Override
        protected List<LegislatorModel> doInBackground(String... urls) {
            List<LegislatorModel> lModelList = new ArrayList<>();
            for(String item:texts){
                //publishProgress(item);
            }
            try {
                URL url = new URL(urls[0]);

                connection =(HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                String line="";
                StringBuffer buffer = new StringBuffer();
                while((line = reader.readLine())!=null){
                    buffer.append(line);
                }
                String finalJson= buffer.toString();
                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("results");

                for(int i=0;i<parentArray.length();i++){
                    LegislatorModel lModel = new LegislatorModel();

                    JSONObject results = parentArray.getJSONObject(i);
                    String party = results.getString("party");
                    String bioguide_id = results.getString("bioguide_id");
                    String name = results.getString("last_name")+", "+results.getString("first_name");
                    String district = results.getString("district");
                    if(district.equals("null")) district="0";
                    String state = results.getString("state_name");
                    lModel.setBioguide_id(bioguide_id);
                    lModel.setDistrict(district);
                    lModel.setParty(party);
                    lModel.setName(name);
                    lModel.setState(state);
                    lModel.setFullname(results.getString("title")+". "+name);
                    String email =results.getString("oc_email");
                    lModel.setChamber(results.getString("chamber"));

                    lModel.setEmail(email.equals("null")?"N.A.":email);
                    lModel.setContact(results.getString("phone"));
                    lModel.setStartTerm(results.getString("term_start"));
                    lModel.setEndTerm(results.getString("term_end"));
                    lModel.setOffice(results.getString("office"));
                    lModel.setSt(results.getString("state"));
                    lModel.setFax(results.getString("fax").equals("null")?"N.A.":results.getString("fax"));
                    lModel.setBirth(results.getString("birthday"));
                    lModel.setTitle(results.getString("title"));

                    if(results.has("twitter_id")&&!results.isNull("twitter_id")){
                        lModel.setTwitter(results.getString("twitter_id"));
                    }else{
                        lModel.setTwitter("None");
                    }
                    if(results.has("facebook_id")&&!results.isNull("facebook_id")){
                        lModel.setFacebook(results.getString("facebook_id"));
                    }else{
                        lModel.setFacebook("None");
                    }
                    if(results.has("website")&&!results.isNull("website")){
                        lModel.setWebsite(results.getString("website"));
                    }else{
                        lModel.setWebsite("None");
                    }

                    lModelList.add(lModel);

                }
                //publishProgress(buffer.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection!=null) {
                    connection.disconnect();
                }
                try {
                    if(reader!=null) reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return lModelList;
        }

        @Override
        protected void onProgressUpdate(String... values) {


        }

        @Override
        protected void onPostExecute(List<LegislatorModel> result) {
            //TODO need to set data to the list
            Activity myActivity = getActivity();
            if (myActivity==null) return;
            LegislatorAdapter adapter = new LegislatorAdapter(myActivity.getApplicationContext(),R.layout.legrow,result);
            adapter.sort(new Comparator<LegislatorModel>() {
                @Override
                public int compare(LegislatorModel l1, LegislatorModel l2) {
                    String s1 =l1.getName(), s2 = l2.getName();
                    int i1 =s1.indexOf(','), i2 = s2.indexOf(',');
                    return s1.substring(0,i1).compareTo(s2.substring(0,i2));

                }
            });
            senateList.setAdapter(adapter);
            getSenateIndex(adapter.legList);
            LinearLayout sideLayout = (LinearLayout)getActivity().findViewById(R.id.senateside);
            displayIndex(sideLayout);
        }

    }
    class LegislatorAdapter extends ArrayAdapter{
               public List<LegislatorModel> legList;
        private int resource;
        private LayoutInflater inflater;
        private Context context;
        public LegislatorAdapter(Context context, int resource, List<LegislatorModel> objects) {
            super(context, resource, objects);
            legList =objects;
            this.resource=resource;
            this.context=context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView =inflater.inflate(resource,null);
            }
            ImageView photo;
            TextView tvName;
            TextView tvPSD;
            photo= (ImageView)convertView.findViewById(R.id.legphoto);
            tvName = (TextView)convertView.findViewById(R.id.legn);
            tvPSD = (TextView)convertView.findViewById(R.id.legpsd);
            Picasso.with(context).load(legList.get(position).getPhoto()).resize(67,82).into(photo);

            tvName.setText(legList.get(position).getName());
            tvPSD.setText("("+legList.get(position).getParty()+")"+legList.get(position).getState()+" - District: "+legList.get(position).getDistrict());
            return convertView;
        }

    }
    @Override
    public void onTabChanged(String s) {

    }
}

