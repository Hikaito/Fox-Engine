package system.backbone;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VersionNumber implements Comparable<VersionNumber> {

    @Expose
    @SerializedName(value="number")
    private int[] versionNumber;

    // initialize new version number
    public VersionNumber(){
        versionNumber = new int[1];
        versionNumber[0] = 0;
    }

    // generate version number from string
    public VersionNumber(String versionNumberString){
        // split into substrings
        String[] portions = versionNumberString.split("\\.");

        // generate version number
        versionNumber = new int[portions.length];

        // populate array
        for(int i = 0; i < portions.length; i++){
            try{
                versionNumber[i] = Integer.parseInt(portions[i]);
            }
            catch(NumberFormatException e){
                versionNumber[i] = 0;
            }
        }
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(versionNumber[0]);
        for(int i=1; i<versionNumber.length; i++){
            builder.append(".").append(versionNumber[i]);
        }
        return builder.toString();
    }

    @Override
    public int compareTo(VersionNumber o) {
        VersionNumber larger = this;
        VersionNumber smaller = o;

        // select larger array
        if (smaller.versionNumber.length > larger.versionNumber.length){
            larger = o;
            smaller = this;
        }

        // check all arrays of smaller
        for(int i = 0; i < smaller.versionNumber.length;i++){
            // check for smaller
            if(this.versionNumber[i] > o.versionNumber[i]){
                return 1;
            }

            // check for larger
            if(this.versionNumber[i] < o.versionNumber[i]){
                return -1;
            }

            //else continue
        }

        // at this point, they are still the same
        // if one is longer, that is considered larger
        if(this.versionNumber.length > o.versionNumber.length){
            // check for zeros
            for(int i = o.versionNumber.length; i < this.versionNumber.length; i++){
                if(this.versionNumber[i] != 0){
                    return -1;
                }
            }
        }
        if(this.versionNumber.length < o.versionNumber.length){
            // check for zeros
            for(int i = this.versionNumber.length; i < o.versionNumber.length; i++){
                if(o.versionNumber[i] != 0){
                    return -1;
                }
            }
        }

        // else at last same
        return 0;
    }
}
