package at.aau.serg.websocketbrokerdemo.model
import at.aau.serg.websocketbrokerdemo.model.Card
import com.example.myapplication.R

data class Card (

    val cardId: String,
   // val name: String,
    val imageResId: Int,
    )
object CardRepository {

    val cards = listOf(
        Card("MRS_LAVENDER", R.drawable.cmrslavender),
        Card("MRS_PINK", R.drawable.cmrspink),
        Card("DR_RED", R.drawable.cdrred),
        Card("DR_BLUE", R.drawable.cdrblue),

        Card("KITCHEN", R.drawable.ckitchen),
        Card("LOUNGE", R.drawable.clounge),
        Card("STUDY", R.drawable.cstudy),
        Card("BALLROOM", R.drawable.cballroom),
        Card("LIBRARY", R.drawable.clieberary),
        Card("BILLIARDROOM", R.drawable.cbilliardroom),

        Card("MEAT_CLEAVER", R.drawable.cmeatcleaver),
        Card("SYRINGE", R.drawable.csyringe),
        Card("AX", R.drawable.cax),
        Card("KNIFE", R.drawable.cknife),
        Card("SHOTGUN", R.drawable.cshotgun)
    )}