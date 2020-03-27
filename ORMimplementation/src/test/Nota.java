package test;

import annotations.Column;
import annotations.PrimaryKey;
import annotations.Table;

@Table(name="Nota")
public class Nota {
      @Column(name="Value")
      public float val;
      @PrimaryKey(name="nid")
      public int nid;
      public Nota(float val) {
	    this.val=val;
      }

}
