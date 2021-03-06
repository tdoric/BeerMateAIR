package hr.foi.air.beermate;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.DecimalFormat;


/**
 * Activity koji sluzi za prikaz i filtiranje modela po filterima
 */
public class BeersListActivity extends AppCompatActivity {

    private RecyclerView beersList;
    private DatabaseReference mReference;
    FirebaseRecyclerAdapter<Beer,BeerViewHolder> firebaseRecyclerAdapter;
    Query query;
    String something="a";
    String userId = "";
    String userRating = "";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beers);
        mReference = FirebaseDatabase.getInstance().getReference().child("beers");
        beersList = (RecyclerView) findViewById(R.id.main_recycler);
        beersList.setHasFixedSize(true);
        beersList.setLayoutManager(new LinearLayoutManager(this));
        userId = getIntentUserId();
        userRating = getIntentUserRating();


    }

    @Override
    protected void onStart() {
        super.onStart();
        showData();
    }

    /**
     * Metoda koja na osnovu
     * @param item
     *postavlja globalno definiranu varijablu koja se koristi u nastavku activitya za potrebe
     * izvodenja drugih metoda
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idItem = item.getItemId();
        switch (idItem){
            case R.id.nameBeer:
                something="a";
                Toast.makeText(getApplicationContext(), R.string.filterName, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.countryBeer:
                something="b";
                Toast.makeText(getApplicationContext(), R.string.filterCountry, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.percentageBeer:
                something="c";
                Toast.makeText(getApplicationContext(), R.string.filterPercentage, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.typeBeer:
                something="d";
                Toast.makeText(getApplicationContext(), R.string.filterType, Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }


    /**
     * Metoda koja postavlja pocetni action bar prema definiranom layoutu
     * te  kada se klikne odnosno mjenja text u dohvacenom searchViewu izvrsava se
     * upit prema primljenoj globalnoj varijabli.
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search,menu);
        MenuItem menuItem = menu.findItem(R.id.menuSearch);
        SearchView searchView =(SearchView) MenuItemCompat.getActionView(menuItem);

            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    showData();
                    return false;
                }
            });

        /**
         * Listener koji promjenom teksta u searcViewu izvršava metode
         */

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {


                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

            /**
             * Metoda koja na osnovu teksta iz searchView-a odabire upit
             * @param newText
             * @return
             */
                @Override
                public boolean onQueryTextChange(String newText) {

                    if (something == "a") {
                        query = addNameCriteria(newText);
                    } else if (something == "b") {

                        query = addCountryCriteria(newText);
                    } else if (something == "c" && !newText.equals("") && newText.matches("\\d+(\\.\\d+)*")) {
                        Double novo =  Double.valueOf(newText);
                        query = addPercentageCriteria(novo);

                    } else if (something == "d") {
                        query = addTypeCriteria(newText);
                    }else{
                        query = mReference.orderByChild("name").equalTo(newText);
                    }

                    /**
                     * Adapter koji sluzi za prikaz rezultata tipa model, BeerViewHolder
                     * te na osnovu ulaznih parametara modela, layouta, holdera i upita prema bazi
                     * prikazuje dobivene modele u cardView-u
                     */
                    firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Beer, BeerViewHolder>(
                            Beer.class, R.layout.beer_row, BeerViewHolder.class, query
                    ) {
                        @Override
                        protected void populateViewHolder(BeerViewHolder viewHolder, Beer model, int position) {

                            viewHolder.setTitle(model.getName());
                            viewHolder.setDesc(model.getStrength());
                            viewHolder.setImage(getApplicationContext(), model.getImage());
                            viewHolder.setCountry(model.getCountry());
                            viewHolder.setBrewery(model.getBrewery());
                            viewHolder.setPercentage(model.getPercentage());


                            model.setUid(this.getRef(position).getKey());

                            DecimalFormat df2 = new DecimalFormat(".#");
                            float average = (float) model.getTotalVotes() / (float) model.getNumberOfVotes();

                            viewHolder.setAverageRate( df2.format(average));
                            viewHolder.setVotes(model.getNumberOfVotes());

                            viewHolder.setRateButton(userRating, model.getUid(), model.getName(), average, model.getTotalVotes(), model.getNumberOfVotes());
                            viewHolder.setShowButton(model.getUid());


                        }
                    };

                    beersList.setAdapter(firebaseRecyclerAdapter);

                    return false;
                }

            });


        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Metoda koja inicijalno se poziva da  bi se prikazali svi modeli iz baze podataka
     */

    public void showData(){



        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Beer, BeerViewHolder>(
                Beer.class,R.layout.beer_row,BeerViewHolder.class,mReference
        ) {
            @Override
            protected void populateViewHolder(BeerViewHolder viewHolder, Beer model, int position) {

                viewHolder.setTitle(model.getName());
                viewHolder.setDesc(model.getStrength());
                viewHolder.setImage(getApplicationContext(),model.getImage());
                viewHolder.setCountry(model.getCountry());
                viewHolder.setBrewery(model.getBrewery());
                viewHolder.setPercentage(model.getPercentage());

                model.setUid(this.getRef(position).getKey());
                DecimalFormat df2 = new DecimalFormat(".#");
                float average = (float) model.getTotalVotes() / (float) model.getNumberOfVotes();
                viewHolder.setAverageRate(df2.format(average));
                viewHolder.setVotes(model.getNumberOfVotes());
                viewHolder.setRateButton(userRating, model.getUid(), model.getName(), average, model.getTotalVotes(), model.getNumberOfVotes());
                viewHolder.setShowButton(model.getUid());

            }
        };

        beersList.setAdapter(firebaseRecyclerAdapter);
    }


    /**
     * Upit za dohvacanje modela prema jakosti piva
     * @param name
     * @return
     */
    public Query addPercentageCriteria(Double name ){
        query = mReference.orderByChild("percentage").equalTo(name);
        return query;
    }

    /**
     * Upit za dohvacanje modela prema imenu
     * @param name
     * @return
     */
    public Query addNameCriteria(String name ){
        query = mReference.orderByChild("name").equalTo(name);
        return query;
    }

    /**
     * Upit za dohvacanje modela prema zemlji porijekla
     * @param name
     * @return
     */
    public Query addCountryCriteria(String name ){
        query = mReference.orderByChild("country").equalTo(name);
        return query;
    }

    /**
     * Upit za dohvacanje modela prema tipu
     * @param name
     * @return
     */
    public Query addTypeCriteria(String name ){
        query = mReference.orderByChild("strength").equalTo(name);
        return query;
    }
    /**
     * Dohvacanje intenta kako bismo znali koji je user logiran u aplikaciju odnosno idUsera
     * @return
     */

    private String getIntentUserId() {
        String userId = "";
        if (getIntent() != null) {
            userId = getIntent().getStringExtra("userId");
        }
        return userId;
    }
    /**
     * Dohvacanje intenta kako bismo znali koje je nacin ocjenivanja logiranog korisnika
     * @return
     */

    private String getIntentUserRating() {
        String userRating = "";
        if (getIntent() != null) {
            userRating = getIntent().getStringExtra("userRating");
        }
        return userRating;
    }



}
