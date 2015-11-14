import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;

public class UTXO {
    private String txid;
    private Integer index;
    private Integer amount;

    public UTXO() {
        this.txid = "";
        this.index = 0;
        this.amount = 0;
    }

    public UTXO(String txid, Integer index, Integer amount) {
        this.txid = txid;
        this.index = index;
        this.amount = amount;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
    public String toString() {
        return String.format("id=%s idx=%d amt=%d", getTxid(), getIndex(), getAmount());
    }

    public DatabaseEntry objectToEntry() {
        TupleOutput o = new TupleOutput();
        DatabaseEntry e = new DatabaseEntry();

        o.writeString(getTxid());
        o.writeInt(getIndex());
        o.writeInt(getAmount());

        TupleBinding.outputToEntry(o, e);
        return e;
    }

    public void entryToObject(DatabaseEntry e) {
        TupleInput i = TupleBinding.entryToInput(e);
        setTxid(i.readString());
        setIndex(i.readInt());
        setAmount(i.readInt());
    }
}
