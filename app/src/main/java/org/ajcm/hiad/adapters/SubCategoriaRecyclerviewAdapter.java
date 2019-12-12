package org.ajcm.hiad.adapters;

import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ajcm.hiad.CallbackFragments;
import org.ajcm.hiad.R;
import org.ajcm.hiad.fragments.ContenidoFragment;
import org.ajcm.hiad.fragments.ContenidoMainFragment;
import org.ajcm.hiad.models.Himno2008;
import org.zakariya.stickyheaders.SectioningAdapter;

import java.util.ArrayList;

/**
 * Created by jhonlimaster on 03-10-17.
 */

public class SubCategoriaRecyclerviewAdapter extends SectioningAdapter {

    private static final String TAG = "SubCategoriaRecyclervie";
    private ArrayList<ContenidoFragment.Section> sections;
    private FragmentActivity fragmentActivity;
    private CallbackFragments callbackFragments;

    public SubCategoriaRecyclerviewAdapter(FragmentActivity context, ArrayList<ContenidoFragment.Section> sections) {
        this.sections = sections;
        this.fragmentActivity = context;
        callbackFragments = (CallbackFragments) fragmentActivity;
    }

    @Override
    public GhostHeaderViewHolder onCreateGhostHeaderViewHolder(ViewGroup parent) {
        final View ghostView = new View(parent.getContext());
        ghostView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new GhostHeaderViewHolder(ghostView);
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_item_item, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_item_header, parent, false);
        Log.e(TAG, "onCreateHeaderViewHolder: ");
        return new HeaderViewHolder(v);
    }

    @Override
    public void onBindItemViewHolder(SectioningAdapter.ItemViewHolder viewHolder, int sectionIndex, int itemIndex, int itemType) {
        ItemViewHolder ivh = (ItemViewHolder) viewHolder;
        final Himno2008 himno2008 = sections.get(sectionIndex).getHimno2008s().get(itemIndex);
        ivh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbackFragments.callbackOK(ContenidoMainFragment.class, himno2008);
            }
        });
        ivh.textView.setText(himno2008.getNumero() + " " + himno2008.getTitulo());
    }

    @Override
    public void onBindHeaderViewHolder(SectioningAdapter.HeaderViewHolder viewHolder, int sectionIndex, int headerType) {
        HeaderViewHolder hvh = (HeaderViewHolder) viewHolder;
        hvh.textView.setText(sections.get(sectionIndex).getHeaders().toUpperCase());
        Log.e(TAG, "onBindHeaderViewHolder: " + sections.get(sectionIndex).getHeaders().toUpperCase());
    }

    @Override
    public boolean doesSectionHaveHeader(int sectionIndex) {
//        Log.e(TAG, "doesSectionHaveHeader: " + TextUtils.isEmpty(sections.get(sectionIndex).getHeaders()));
        return !TextUtils.isEmpty(sections.get(sectionIndex).getHeaders());
//        return false;
    }

    public class ItemViewHolder extends SectioningAdapter.ItemViewHolder {
        TextView textView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.subTextView);
        }
    }

    public class HeaderViewHolder extends SectioningAdapter.HeaderViewHolder {
        TextView textView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.headerTextView);
        }
    }

    @Override
    public int getNumberOfSections() {
        return sections.size();
    }

    @Override
    public int getNumberOfItemsInSection(int sectionIndex) {
        return sections.get(sectionIndex).getHimno2008s().size();
    }
}
