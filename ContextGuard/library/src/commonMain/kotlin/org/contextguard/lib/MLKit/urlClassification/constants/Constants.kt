package org.contextguard.lib.MLKit.urlClassification.constants

object Constants {

    val SCALER_MEAN = listOf(
        59.365767921004775,
        9.526309365426162,
        4.748159992188358,
        6.836626835430677,
        0.31025644155305204,
        0.8080899315260774,
        8.004772424904491,
        0.08814949529470639,
        0.15534182035665028,
        2.893139181486409,
        0.01989527517728765,
        0.0006835186563976125,
        24.00541932649001,
        3.1535964066447777,
        2.590755410172222,
        7.546729485286041,
        0.9438538246530532,
        1.1823896300455272,
        0.0898460862454076,
        0.010594539174162995,
        0.007933698690329432,
        19.58119835467295,
        2.2568565465219885,
        0.9656775988966056,
        0.9716461814497919
    )

    val SCALER_SCALE = listOf(
        53.216747273243,
        5.75769608577285,
        10.19248811680159,
        8.239638398336991,
        0.7114613104430088,
        3.330046018237877,
        4.366019766209441,
        0.35781304710301276,
        0.9320685494310716,
        0.629164551979009,
        0.14402928040080856,
        0.0345787572209034,
        30.631009707100556,
        3.289156723433659,
        5.530899221455673,
        39.08805140325748,
        4.01853978241549,
        7.467734672265191,
        2.2575226221885076,
        0.39462641751198413,
        0.3722817456597407,
        9.112627205536725,
        0.8758910513849973,
        3.544093829767996,
        0.16598156380703843
    )

    val URL_FEATURE_KEYS = listOf(
        "url_length",
        "url_punctuations_count",
        "url_digits_count",
        "subdomain_length",
        "subdomain_punctuations_count",
        "subdomain_digits_count",
        "domain_length",
        "domain_punctuations_count",
        "domain_digits_count",
        "tld_length",
        "tld_punctuations_count",
        "tld_digits_count",
        "path_length",
        "path_punctuations_count",
        "path_digits_count",
        "query_length",
        "query_punctuations_count",
        "query_digits_count",
        "fragment_length",
        "fragment_punctuations_count",
        "fragment_digits_count",
        "netloc_length",
        "netloc_punctuations_count",
        "netloc_digits_count",
        "secured_scheme"
    )

    const val MODEL_INPUT_SIZE = 25
}