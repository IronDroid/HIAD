package org.ajcm.hiad.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ajcm.hiad.R;
import org.ajcm.hiad.fragments.ContenidoFragment;
import org.ajcm.hiad.models.Himno2008;
import org.zakariya.stickyheaders.SectioningAdapter;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by jhonlimaster on 03-10-17.
 */

public class SubCategoriaRecyclerviewAdapter extends SectioningAdapter {

    private static final String TAG = "SubCategoriaRecyclervie";
    private ArrayList<ContenidoFragment.Section> sections;
    private FragmentActivity fragmentActivity;

    public SubCategoriaRecyclerviewAdapter(FragmentActivity context, ArrayList<ContenidoFragment.Section> sections) {
        this.sections = sections;
        this.fragmentActivity = context;
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
        return new HeaderViewHolder(v);
    }

    @Override
    public void onBindItemViewHolder(SectioningAdapter.ItemViewHolder viewHolder, int sectionIndex, int itemIndex, int itemType) {
        ItemViewHolder ivh = (ItemViewHolder) viewHolder;
        final Himno2008 himno2008 = sections.get(sectionIndex).getHimno2008s().get(itemIndex);
        ivh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentActivity.setResult(RESULT_OK, new Intent().putExtra("numero", himno2008.getNumero()));
                fragmentActivity.finish();
            }
        });
        ivh.textView.setText(himno2008.getNumero() + " " + himno2008.getTitulo());
    }

    @Override
    public void onBindHeaderViewHolder(SectioningAdapter.HeaderViewHolder viewHolder, int sectionIndex, int headerType) {
        HeaderViewHolder hvh = (HeaderViewHolder) viewHolder;
        hvh.textView.setText(sections.get(sectionIndex).getHeaders().toUpperCase());
    }

    @Override
    public boolean doesSectionHaveHeader(int sectionIndex) {
        return !TextUtils.isEmpty(sections.get(sectionIndex).getHeaders());
    }

    public class ItemViewHolder extends SectioningAdapter.ItemViewHolder implements View.OnClickListener {
        TextView textView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);
            this.itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.e(TAG, "onClick: item");
        }
    }

    public class HeaderViewHolder extends SectioningAdapter.HeaderViewHolder {
        TextView textView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);
        }

//        void updateSectionCollapseToggle(boolean sectionIsCollapsed) {
//            @DrawableRes int id = sectionIsCollapsed
//                    ? R.drawable.ic_expand_more_black_24dp
//                    : R.drawable.ic_expand_less_black_24dp;
//
//            collapseButton.setImageDrawable(ContextCompat.getDrawable(collapseButton.getContext(), id));
//        }
    }

    @Override
    public int getNumberOfSections() {
        Log.e(TAG, "getNumberOfSections: " + sections.size());
        return sections.size();
    }

    @Override
    public int getNumberOfItemsInSection(int sectionIndex) {
        Log.e(TAG, "getNumberOfItemsInSection: " + sections.get(sectionIndex));
        return sections.get(sectionIndex).getHimno2008s().size();
    }
}
