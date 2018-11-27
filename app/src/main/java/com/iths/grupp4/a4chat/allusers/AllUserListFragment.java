package com.iths.grupp4.a4chat.allusers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iths.grupp4.a4chat.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class AllUserListFragment extends Fragment{
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference allUsers = db.collection("users");

    private AllUserAdapter mUserAdapter;
    public SearchView search_users;

    public AllUserListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_all_user_list, container, false);

        Query query = allUsers;

        FirestoreRecyclerOptions<AllUsers> recyclerOptions = new FirestoreRecyclerOptions.Builder<AllUsers>()
                .setQuery(query, AllUsers.class)
                .build();

        mUserAdapter = new AllUserAdapter(recyclerOptions);
        RecyclerView recyclerView = view.findViewById(R.id.allUser_listView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mUserAdapter);

        onUserClick();

        search_users = view.findViewById(R.id.searchUsers);

        search_users.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                //Returning the recyclerview to its original view. If user types letter, then delets letter.
                if (s.trim().isEmpty()){
                    getList(allUsers);
                    onUserClick();
                    mUserAdapter.startListening();
                    //Getting the name and saving it in searchQuery, then setting it in getList
                }else {
                    CollectionReference usersRef = db.collection("users");
                    Query searchQuery = usersRef.orderBy("name").startAt(s.trim()).endAt(s.trim() +"\uf8ff");
                    getList(searchQuery);
                    onUserClick();
                    mUserAdapter.startListening();
                }
                return false;
            }
        });
        return view;
    }

    //Sets the recyclerview with a new query
    private void getList(Query q) {

        FirestoreRecyclerOptions<AllUsers> recyclerOptions = new FirestoreRecyclerOptions.Builder<AllUsers>()
                .setQuery(q, AllUsers.class)
                .build();

        mUserAdapter = new AllUserAdapter(recyclerOptions);
        RecyclerView recyclerView = getView().findViewById(R.id.allUser_listView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mUserAdapter);
    }
    //Handles click to users viewcard
    public void onUserClick (){
        mUserAdapter.setOnItemClickListener(new AllUserAdapter.OnItemClicklistener() {
            @Override
            public void onItemClick(DocumentSnapshot snapshot, int position) {
                AllUsers allUsers = snapshot.toObject(AllUsers.class);
                String id = snapshot.getId();
                Bundle bundle = new Bundle();
                bundle.putString("visit_user_id", id);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                AllUserProfileFragment fragment = new AllUserProfileFragment();
                fragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.frameLayout, fragment);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mUserAdapter.startListening();
    }
}