import HeroSection from '@/components/HeroSection'
import SearchSection from '@/components/SearchSection'
import CarCatalog from '@/components/CarCatalog'
import Footer from '@/components/Footer'

export default function Home() {
  return (
    <main className="min-h-screen bg-white">
      <HeroSection />
      <SearchSection />
      <CarCatalog />
      <Footer />
    </main>
  )
}
