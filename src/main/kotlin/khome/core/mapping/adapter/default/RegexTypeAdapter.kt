package khome.core.mapping.adapter.default

import khome.core.mapping.KhomeTypeAdapter

class RegexTypeAdapter : KhomeTypeAdapter<Regex> {
    override fun <P> from(value: P): Regex {
        return Regex(value as String)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P> to(value: Regex): P {
        return value.toString() as P
    }
}
