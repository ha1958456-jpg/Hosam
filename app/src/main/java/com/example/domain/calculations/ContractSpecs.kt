package com.example.domain.calculations

enum class AssetCategory(val arabicLabel: String) {
    GOLD("ذهب ومعادن"),
    FOREX("عملات أجنبية"),
    CRYPTO("عملات رقمية"),
    INDICES("مؤشرات"),
    STOCKS("أسهم"),
    OTHER("أصول أخرى")
}

data class SymbolPreset(
    val symbol: String,
    val nameArabic: String,
    val category: AssetCategory,
    val defaultContractSize: Double,
    val pipMultiplier: Double,
    val typicalSpread: Double
)

object ContractSpecs {
    val PRESETS = listOf(
        SymbolPreset("XAUUSD", "الذهب مقابل الدولار", AssetCategory.GOLD, 100.0, 1.0, 0.20),
        SymbolPreset("XAGUSD", "الفضة مقابل الدولار", AssetCategory.GOLD, 5000.0, 0.01, 0.03),
        SymbolPreset("EURUSD", "اليورو / دولار", AssetCategory.FOREX, 100000.0, 0.0001, 0.00010),
        SymbolPreset("GBPUSD", "الجنيه الإسترليني / دولار", AssetCategory.FOREX, 100000.0, 0.0001, 0.00015),
        SymbolPreset("USDJPY", "الدولار / ين ياباني", AssetCategory.FOREX, 100000.0, 0.01, 0.012),
        SymbolPreset("AUDUSD", "الدولار الأسترالي / دولار", AssetCategory.FOREX, 100000.0, 0.0001, 0.00012),
        SymbolPreset("USDCAD", "الدولار / دولار كندي", AssetCategory.FOREX, 100000.0, 0.0001, 0.00014),
        SymbolPreset("USDCHF", "الدولار / فرنك سويسري", AssetCategory.FOREX, 100000.0, 0.0001, 0.00015),
        SymbolPreset("BTCUSD", "البيتكوين مقابل الدولار", AssetCategory.CRYPTO, 1.0, 1.0, 15.0),
        SymbolPreset("ETHUSD", "الإيثيريوم مقابل الدولار", AssetCategory.CRYPTO, 1.0, 1.0, 1.5),
        SymbolPreset("SOLUSD", "سولانا مقابل الدولار", AssetCategory.CRYPTO, 1.0, 0.1, 0.08),
        SymbolPreset("US30", "مؤشر داو جونز الصناعي", AssetCategory.INDICES, 1.0, 1.0, 2.0),
        SymbolPreset("NAS100", "مؤشر ناسداك 100", AssetCategory.INDICES, 1.0, 1.0, 1.5),
        SymbolPreset("SPX500", "مؤشر ستاندرد آند بورز 500", AssetCategory.INDICES, 1.0, 1.0, 0.4),
        SymbolPreset("GER40", "مؤشر داكس الألماني", AssetCategory.INDICES, 1.0, 1.0, 1.2),
        SymbolPreset("AAPL", "أسهم آبل Apple", AssetCategory.STOCKS, 100.0, 0.01, 0.05),
        SymbolPreset("NVDA", "أسهم إنفيديا NVIDIA", AssetCategory.STOCKS, 100.0, 0.01, 0.08),
        SymbolPreset("TSLA", "أسهم تسلا Tesla", AssetCategory.STOCKS, 100.0, 0.01, 0.10)
    )

    fun findPreset(symbol: String): SymbolPreset? {
        val clean = symbol.trim().uppercase().replace("/", "")
        return PRESETS.firstOrNull { it.symbol == clean || it.symbol.replace("/", "") == clean }
    }

    fun getContractSize(symbol: String, category: String): Double {
        val preset = findPreset(symbol)
        if (preset != null) return preset.defaultContractSize
        return when (category.uppercase()) {
            "GOLD" -> 100.0
            "FOREX" -> 100000.0
            "CRYPTO" -> 1.0
            "INDICES" -> 1.0
            "STOCKS" -> 100.0
            else -> 100.0
        }
    }
}
