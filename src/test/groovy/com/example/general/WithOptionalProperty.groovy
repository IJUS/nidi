package com.example.general

import net.ijus.nidi.Optional

/**
 * Created by pfried on 9/14/14.
 */
class WithOptionalProperty {

    String prop;

    WithOptionalProperty(@Optional("optionalProperty") String prop) {
        this.prop = prop
    }

}
