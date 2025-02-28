import com.google.gson.annotations.SerializedName

data class ProjectResponse(
    val status: String,
    val result: ProjectsResult, // Changed from 'Project' to 'ProjectsResult'
    @SerializedName("request_id") val requestId: String
)
data class ProjectDetailsResponse(
    val result: ProjectDetailsResult
)

data class ProjectDetailsResult(
    val project: Project
)
data class ProjectsResult(
    @SerializedName("projects") val projects: List<Project> // List of projects
)

data class Project(
    val id: Int = 0, // Default value
    @SerializedName("owner_id") val ownerId: Int = 0, // Default value
    val title: String = "", // Default value
    val status: String = "", // Default value
    @SerializedName("sub_status") val subStatus: String = "", // Default value
    @SerializedName("seo_url") val seoUrl: String = "", // Default value
    val currency: Currency = Currency(), // Default value
    val description: String? = null, // Default value
    val jobs: List<String>? = null, // Default value
    val submitdate: Long = 0, // Default value
    @SerializedName("preview_description") val previewDescription: String = "", // Default value
    val deleted: Boolean = false, // Default value
    val nonpublic: Boolean = false, // Default value
    val hidebids: Boolean = false, // Default value
    val type: String = "", // Default value
    val bidperiod: Int = 0, // Default value
    val budget: Budget? = null, // Default value
    val bid_stats: BidStats = BidStats(), // Default value
    val upgrades: Upgrades = Upgrades(), // Default value
    val language: String = "", // Default value
    val location: Location = Location(), // Default value
    val local: Boolean = false, // Default value
    val pool_ids: List<String> = emptyList(), // Default value
    @SerializedName("enterprise_ids") val enterpriseIds: List<String> = emptyList(), // Default value
    @SerializedName("is_escrow_project") val isEscrowProject: Boolean = false, // Default value
    @SerializedName("is_seller_kyc_required") val isSellerKycRequired: Boolean = false, // Default value
    @SerializedName("is_buyer_kyc_required") val isBuyerKycRequired: Boolean = false // Default value
) {
    // Add a no-argument constructor
    constructor() : this(
        0, 0, "", "", "", "", Currency(), null, null, 0, "", false, false, false, "", 0, null, BidStats(), Upgrades(), "", Location(), false, emptyList(), emptyList(), false, false, false
    )
}

data class Currency(
    val id: Int = 0,
    val code: String = "",
    val sign: String = "",
    val name: String = "",
    @SerializedName("exchange_rate") val exchangeRate: Double = 0.0,
    val country: String = "",
    @SerializedName("is_external") val isExternal: Boolean = false,
    @SerializedName("is_escrowcom_supported") val isEscrowcomSupported: Boolean = false
) {
    // Add a no-argument constructor
    constructor() : this(0, "", "", "", 0.0, "", false, false)
}

data class Budget(
    val minimum: Double? = null, // Default value
    val maximum: Double? = null, // Default value
    val name: String? = null, // Default value
    @SerializedName("project_type") val projectType: String? = null, // Default value
    @SerializedName("currency_id") val currencyId: Int? = null // Default value
) {
    // Add a no-argument constructor
    constructor() : this(null, null, null, null, null)
}

data class BidStats(
    @SerializedName("bid_count") val bidCount: Int = 0, // Default value
    @SerializedName("bid_avg") val bidAvg: Double = 0.0 // Default value
) {
    // Add a no-argument constructor
    constructor() : this(0, 0.0)
}

data class Upgrades(
    val featured: Boolean = false, // Default value
    val sealed: Boolean = false, // Default value
    val nonpublic: Boolean = false, // Default value
    val fulltime: Boolean = false, // Default value
    val urgent: Boolean = false, // Default value
    val qualified: Boolean = false, // Default value
    @SerializedName("NDA") val nda: Boolean = false, // Default value
    @SerializedName("ip_contract") val ipContract: Boolean = false, // Default value
    @SerializedName("success_bundle") val successBundle: Boolean? = null, // Default value
    @SerializedName("non_compete") val nonCompete: Boolean = false, // Default value
    @SerializedName("project_management") val projectManagement: Boolean = false, // Default value
    @SerializedName("pf_only") val pfOnly: Boolean = false, // Default value
    val recruiter: Boolean? = null // Default value
) {
    // Add a no-argument constructor
    constructor() : this(false, false, false, false, false, false, false, false, null, false, false, false, null)
}

data class Location(
    val country: Country? = null, // Default value
    val city: String? = null, // Default value
    val latitude: Double? = null, // Default value
    val longitude: Double? = null, // Default value
    val timezone: Timezone? = null // Default value
) {
    // Add a no-argument constructor
    constructor() : this(null, null, null, null, null)
}

data class Country(
    val name: String? = null, // Default value
    @SerializedName("flag_url") val flagUrl: String? = null, // Default value
    val code: String? = null, // Default value
    @SerializedName("iso3") val iso3: String? = null // Default value
) {
    // Add a no-argument constructor
    constructor() : this(null, null, null, null)
}

data class Timezone(
    val id: String? = null, // Default value
    val country: String? = null, // Default value
    val timezone: String? = null, // Default value
    val offset: Int? = null // Default value
) {
    // Add a no-argument constructor
    constructor() : this(null, null, null, null)
}