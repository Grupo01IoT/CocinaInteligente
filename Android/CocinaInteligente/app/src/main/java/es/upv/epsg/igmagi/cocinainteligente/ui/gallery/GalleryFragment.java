package es.upv.epsg.igmagi.cocinainteligente.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.upv.epsg.igmagi.cocinainteligente.R;
import es.upv.epsg.igmagi.cocinainteligente.adapter.YoutubeRecyclerAdapter;
import es.upv.epsg.igmagi.cocinainteligente.model.YoutubeVideo;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class GalleryFragment extends Fragment {
    @BindView(R.id.recyclerViewFeed) RecyclerView recyclerViewFeed;

    View root;
    private YoutubeRecyclerAdapter mRecyclerAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_gallery, container, false);
recyclerViewFeed = root.findViewById(R.id.recyclerViewFeed);
        ButterKnife.bind(getActivity()); // prepare data for list
        List<YoutubeVideo> youtubeVideos = prepareList();
        mRecyclerAdapter = new YoutubeRecyclerAdapter(youtubeVideos);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewFeed.setLayoutManager(mLayoutManager);
        recyclerViewFeed.setItemAnimator(new DefaultItemAnimator());
        recyclerViewFeed.setAdapter(mRecyclerAdapter);
        return root;
    }
    private List<YoutubeVideo> prepareList() {
        ArrayList<YoutubeVideo> videoArrayList=new ArrayList<>();
        // add first item
        YoutubeVideo video1 = new YoutubeVideo();
        video1.setId(1l);
        video1.setImageUrl("https://i.ytimg.com/vi/aVtGoRZIJXg/hqdefault.jpg?sqp=-oaymwEZCNACELwBSFXyq4qpAwsIARUAAIhCGAFwAQ==&rs=AOn4CLBK6BJdaVVTnhJM-HgsYV66j48zKg");
        video1.setTitle(
                "Trucos para una tortilla de patatas exquisita");
        video1.setVideoId("aVtGoRZIJXg");
        // add second item
        YoutubeVideo video2 = new YoutubeVideo();
        video2.setId(2l);
        video2.setImageUrl("https://i.ytimg.com/vi/d-LjBUAsNAs/hqdefault.jpg?sqp=-oaymwEZCNACELwBSFXyq4qpAwsIARUAAIhCGAFwAQ==&rs=AOn4CLCexWg0DkaG0eF0vDWUlmeZEvvUMw");
        video2.setTitle(
                "Diferentes maneras de cocinar la pasta ¿Cual eliges tu?");
        video2.setVideoId("d-LjBUAsNAs");
        // add third item
        YoutubeVideo video3 = new YoutubeVideo();
        video3.setId(3l);
        video3.setImageUrl("https://i.ytimg.com/vi/dI-272Zqnig/hqdefault.jpg?sqp=-oaymwEZCPYBEIoBSFXyq4qpAwsIARUAAIhCGAFwAQ==&rs=AOn4CLCABUrtClsES4t5lf69z-eYvehErg");
        video3.setTitle("Hamburguesa estilo Tommy Mel's");
        video3.setVideoId("dI-272Zqnig");
        // add four item
        YoutubeVideo video4 = new YoutubeVideo();
        video4.setId(4l);
        video4.setImageUrl("https://i.ytimg.com/vi/A583r-uten0/hqdefault.jpg?sqp=-oaymwEZCPYBEIoBSFXyq4qpAwsIARUAAIhCGAFwAQ==&rs=AOn4CLBYp-g1YVFyEQpq3zmOmqlGUxFmGw");
        video4.setTitle("¡Explota tu lado asiatico! Truco para hacer Nigiris de Salmón Sushi Fácil y Rápido");
        video4.setVideoId("A583r-uten0");
        // add four item
        YoutubeVideo video5 = new YoutubeVideo();
        video5.setId(5l);
        video5.setImageUrl("https://i.ytimg.com/vi/bB7anyLrUHs/hqdefault.jpg?sqp=-oaymwEZCPYBEIoBSFXyq4qpAwsIARUAAIhCGAFwAQ==&rs=AOn4CLCJIWX50DdlxeE5tVmYPK2d_oTfgg");
        video5.setTitle("Como hacer una rica salsa Tartara!");
        video5.setVideoId("bB7anyLrUHs");
        videoArrayList.add(video1);
        videoArrayList.add(video5);
        videoArrayList.add(video2);
        videoArrayList.add(video3);
        videoArrayList.add(video4);
        return videoArrayList;
    }
}